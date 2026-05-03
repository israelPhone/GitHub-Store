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
                            "What's new sheet — see highlights right after every update.",
                            "APK Inspect: peek inside any release before installing.",
                            "Manual rescan surfaces every GitHub-style app on device.",
                        ),
                    ),
                    WhatsNewSection(
                        type = WhatsNewSectionType.IMPROVED,
                        bullets = listOf(
                            "Sui silent install support, alongside Shizuku.",
                            "Faithful pending row — parked APKs now carry their own icon and version.",
                            "Auth resilience: token-scoped 401 debounce stops spurious sign-outs.",
                        ),
                    ),
                    WhatsNewSection(
                        type = WhatsNewSectionType.FIXED,
                        bullets = listOf(
                            "Multi-source download race no longer corrupts the final APK.",
                            "Shizuku-fallback installs no longer mark apps installed before they actually are.",
                        ),
                    ),
                ),
            ),
        )
    }.sortedByDescending { it.versionCode }

    fun forVersionCode(versionCode: Int): WhatsNewEntry? =
        all.firstOrNull { it.versionCode == versionCode }
}
