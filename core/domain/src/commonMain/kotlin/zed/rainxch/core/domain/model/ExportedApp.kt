package zed.rainxch.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportedApp(
    val packageName: String,
    val repoOwner: String,
    val repoName: String,
    val repoUrl: String,
    // Monorepo tracking (added in export schema v2). Defaults keep
    // old v1 JSON files decoding without changes.
    val assetFilterRegex: String? = null,
    val fallbackToOlderReleases: Boolean = false,
)

@Serializable
data class ExportedAppList(
    /**
     * Export schema version. Bumped to 2 when monorepo fields were added
     * to [ExportedApp]. Older v1 exports still decode correctly because
     * the new fields have safe defaults.
     */
    val version: Int = 2,
    val exportedAt: Long = 0L,
    val apps: List<ExportedApp> = emptyList(),
)
