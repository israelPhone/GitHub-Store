package zed.rainxch.core.domain.system

data class ScanResult(
    val totalCandidates: Int,
    val newCandidates: Int,
    val autoLinked: Int,
    val pendingReview: Int,
    val durationMillis: Long,
    val permissionGranted: Boolean,
)

data class ImportSummary(
    val attempted: Int,
    val linked: Int,
    val failed: Int,
)
