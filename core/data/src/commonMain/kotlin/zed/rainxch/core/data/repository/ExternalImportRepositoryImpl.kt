package zed.rainxch.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import zed.rainxch.core.data.local.db.dao.ExternalLinkDao
import zed.rainxch.core.data.local.db.entities.ExternalLinkEntity
import zed.rainxch.core.domain.repository.ExternalImportRepository
import zed.rainxch.core.domain.system.ExternalAppCandidate
import zed.rainxch.core.domain.system.ExternalAppScanner
import zed.rainxch.core.domain.system.ExternalLinkState
import zed.rainxch.core.domain.system.ImportSummary
import zed.rainxch.core.domain.system.RepoMatchResult
import zed.rainxch.core.domain.system.RepoMatchSource
import zed.rainxch.core.domain.system.ScanResult

class ExternalImportRepositoryImpl(
    private val scanner: ExternalAppScanner,
    private val externalLinkDao: ExternalLinkDao,
    private val preferences: DataStore<Preferences>,
) : ExternalImportRepository {
    // Snapshot cache survives only for the lifetime of the process. Decisions
    // (linked / skipped / never-ask) are persisted in `external_links`; the
    // raw candidate metadata (label, fingerprint, hint) is regenerated on the
    // next scan rather than persisted to keep the schema small.
    private val candidateSnapshot = MutableStateFlow<Map<String, ExternalAppCandidate>>(emptyMap())

    override fun pendingCandidatesFlow(): Flow<List<ExternalAppCandidate>> =
        combine(
            candidateSnapshot,
            externalLinkDao.observePendingReview(),
        ) { snapshot, pendingRows ->
            val pendingPackages = pendingRows.map { it.packageName }.toSet()
            pendingPackages.mapNotNull { snapshot[it] }
        }

    override fun pendingCandidateCountFlow(): Flow<Int> = externalLinkDao.observePendingReviewCount()

    override suspend fun scheduleInitialScanIfNeeded() {
        val alreadyScanned = preferences.data.first()[INITIAL_SCAN_COMPLETED_AT_KEY] != null
        if (alreadyScanned) return
        runCatching { runFullScan() }
            .onSuccess { markInitialScanComplete() }
            .onFailure { Logger.w(it) { "Initial external scan failed; will retry on next launch." } }
    }

    override suspend fun runFullScan(): ScanResult {
        val started = nowMillis()
        val granted = scanner.isPermissionGranted()
        val candidates = scanner.snapshot()
        candidateSnapshot.value = candidates.associateBy { it.packageName }

        val now = nowMillis()
        var newCandidates = 0
        var pendingReview = 0

        candidates.forEach { candidate ->
            val existing = externalLinkDao.get(candidate.packageName)
            val updated = mergeCandidate(existing, candidate, now)
            if (existing == null) newCandidates++
            if (updated.state == ExternalLinkState.PENDING_REVIEW.name) pendingReview++
            externalLinkDao.upsert(updated)
        }

        return ScanResult(
            totalCandidates = candidates.size,
            newCandidates = newCandidates,
            autoLinked = 0, // wired with backend match resolver in Week 2
            pendingReview = pendingReview,
            durationMillis = nowMillis() - started,
            permissionGranted = granted,
        )
    }

    override suspend fun runDeltaScan(changedPackageNames: Set<String>): ScanResult {
        val started = nowMillis()
        val granted = scanner.isPermissionGranted()
        val now = nowMillis()
        var newCandidates = 0
        var pendingReview = 0
        val deltaCandidates = mutableListOf<ExternalAppCandidate>()

        changedPackageNames.forEach { pkg ->
            val candidate = scanner.snapshotSingle(pkg)
            if (candidate == null) {
                externalLinkDao.deleteByPackageName(pkg)
                return@forEach
            }
            deltaCandidates += candidate
            val existing = externalLinkDao.get(pkg)
            val updated = mergeCandidate(existing, candidate, now)
            if (existing == null) newCandidates++
            if (updated.state == ExternalLinkState.PENDING_REVIEW.name) pendingReview++
            externalLinkDao.upsert(updated)
        }

        if (deltaCandidates.isNotEmpty()) {
            candidateSnapshot.update { current ->
                current.toMutableMap().apply {
                    deltaCandidates.forEach { put(it.packageName, it) }
                }
            }
        }

        return ScanResult(
            totalCandidates = deltaCandidates.size,
            newCandidates = newCandidates,
            autoLinked = 0,
            pendingReview = pendingReview,
            durationMillis = nowMillis() - started,
            permissionGranted = granted,
        )
    }

    override suspend fun resolveMatches(candidates: List<ExternalAppCandidate>): List<RepoMatchResult> =
        // Backend strategy ships in Week 2; manifest-derived matches are already
        // persisted by `runFullScan` directly onto the external_links row, so
        // returning empty here is correct for the manifest-only path.
        emptyList()

    override suspend fun importAutoMatched(matches: List<RepoMatchResult>): ImportSummary {
        notImplemented("importAutoMatched")
    }

    override suspend fun linkManually(
        packageName: String,
        owner: String,
        repo: String,
        source: String,
    ): Result<Unit> {
        notImplemented("linkManually")
    }

    override suspend fun skipPackage(
        packageName: String,
        neverAsk: Boolean,
    ) {
        val existing = externalLinkDao.get(packageName)
        val state = if (neverAsk) ExternalLinkState.NEVER_ASK else ExternalLinkState.SKIPPED
        val now = nowMillis()
        val skipExpiresAt = if (neverAsk) null else now + SKIP_TTL_MILLIS
        val row =
            existing?.copy(
                state = state.name,
                lastReviewedAt = now,
                skipExpiresAt = skipExpiresAt,
            ) ?: ExternalLinkEntity(
                packageName = packageName,
                state = state.name,
                repoOwner = null,
                repoName = null,
                matchSource = null,
                matchConfidence = null,
                signingFingerprint = null,
                installerKind = null,
                firstSeenAt = now,
                lastReviewedAt = now,
                skipExpiresAt = skipExpiresAt,
            )
        externalLinkDao.upsert(row)
    }

    override suspend fun unlink(packageName: String) {
        externalLinkDao.deleteByPackageName(packageName)
        candidateSnapshot.update { it - packageName }
    }

    override suspend fun rescanSinglePackage(packageName: String): RepoMatchResult? {
        notImplemented("rescanSinglePackage")
    }

    override suspend fun syncSigningFingerprintSeed() {
        notImplemented("syncSigningFingerprintSeed")
    }

    override suspend fun pruneExpiredSkips() {
        externalLinkDao.pruneExpiredSkips(nowMillis())
    }

    override suspend fun isPermissionGranted(): Boolean = scanner.isPermissionGranted()

    private suspend fun markInitialScanComplete() {
        preferences.edit { prefs ->
            prefs[INITIAL_SCAN_COMPLETED_AT_KEY] = nowMillis()
        }
    }

    private fun mergeCandidate(
        existing: ExternalLinkEntity?,
        candidate: ExternalAppCandidate,
        now: Long,
    ): ExternalLinkEntity {
        if (existing != null && shouldPreserveDecision(existing, now)) {
            return existing.copy(
                signingFingerprint = candidate.signingFingerprint ?: existing.signingFingerprint,
                installerKind = candidate.installerKind.name,
            )
        }

        val hint = candidate.manifestHint
        return ExternalLinkEntity(
            packageName = candidate.packageName,
            state = ExternalLinkState.PENDING_REVIEW.name,
            repoOwner = hint?.owner ?: existing?.repoOwner,
            repoName = hint?.repo ?: existing?.repoName,
            matchSource = if (hint != null) RepoMatchSource.MANIFEST.name else existing?.matchSource,
            matchConfidence = hint?.confidence ?: existing?.matchConfidence,
            signingFingerprint = candidate.signingFingerprint,
            installerKind = candidate.installerKind.name,
            firstSeenAt = existing?.firstSeenAt ?: now,
            lastReviewedAt = now,
            skipExpiresAt = null,
        )
    }

    private fun shouldPreserveDecision(
        existing: ExternalLinkEntity,
        now: Long,
    ): Boolean =
        when (existing.state) {
            ExternalLinkState.MATCHED.name -> true
            ExternalLinkState.NEVER_ASK.name -> true
            ExternalLinkState.SKIPPED.name -> (existing.skipExpiresAt ?: 0) > now
            else -> false
        }

    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    private fun notImplemented(name: String): Nothing =
        error("ExternalImportRepository.$name is not implemented yet (Week 2/3 of E1).")

    companion object {
        private val INITIAL_SCAN_COMPLETED_AT_KEY = longPreferencesKey("external_import_initial_scan_at")
        private const val SKIP_TTL_MILLIS: Long = 7L * 24 * 60 * 60 * 1000
    }
}
