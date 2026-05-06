package zed.rainxch.core.domain.repository

import zed.rainxch.core.domain.model.WhatsNewEntry

interface WhatsNewLoader {
    /**
     * Optional explicit BCP-47 [languageTag] (e.g. `"zh-CN"`, `"fr"`,
     * `null`). When non-null, this takes precedence over the global
     * `LocalizationManager` lookup — used to defeat the race where the
     * `getAppLanguage()` flow fans out to multiple subscribers and the
     * loader runs before the global `Locale.getDefault()` has caught up
     * with the user's just-picked language. Null falls back to
     * whatever `LocalizationManager.getCurrentLanguageCode()` returns
     * at call time (suitable for paths where the tag isn't known
     * upfront, e.g. a deep-linked one-shot load).
     */
    suspend fun loadAll(languageTag: String? = null): List<WhatsNewEntry>

    suspend fun forVersionCode(versionCode: Int, languageTag: String? = null): WhatsNewEntry?
}
