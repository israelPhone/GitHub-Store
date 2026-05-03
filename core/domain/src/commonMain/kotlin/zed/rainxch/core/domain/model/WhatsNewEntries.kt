package zed.rainxch.core.domain.model

object WhatsNewEntries {
    val all: List<WhatsNewEntry> = buildList {
        add(
            WhatsNewEntry(
                versionCode = 15,
                versionName = "1.8.0",
                releaseDate = "2026-05-03",
                sections = listOf(
                    WhatsNewSection(
                        type = WhatsNewSectionType.NEW,
                        bullets = listOf(
                            "APK Inspect — peek inside any release before installing.",
                            "Apps screen now groups updates, pending installs, and installed apps into sections.",
                        ),
                    ),
                    WhatsNewSection(
                        type = WhatsNewSectionType.IMPROVED,
                        bullets = listOf(
                            "Manual rescan surfaces every GitHub-style app on device, not just verified ones.",
                            "Tighter auth handling — transient 401s no longer trigger spurious sign-outs.",
                            "Faithful pending rows — parked APKs carry their own icon and version.",
                        ),
                    ),
                    WhatsNewSection(
                        type = WhatsNewSectionType.FIXED,
                        bullets = listOf(
                            "Multi-source downloads no longer clobber each other's APK file.",
                            "Shizuku-fallback installs no longer flip rows to \"installed\" before the system confirms.",
                            "Self-update no longer leaves apps stuck on \"Preparing to install\".",
                        ),
                    ),
                ),
            ),
        )
    }.sortedByDescending { it.versionCode }

    fun forVersionCode(versionCode: Int): WhatsNewEntry? =
        all.firstOrNull { it.versionCode == versionCode }
}
