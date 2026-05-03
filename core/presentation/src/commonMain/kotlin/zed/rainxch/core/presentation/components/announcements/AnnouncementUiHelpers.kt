package zed.rainxch.core.presentation.components.announcements

import org.jetbrains.compose.resources.StringResource
import zed.rainxch.core.domain.model.AnnouncementCategory
import zed.rainxch.githubstore.core.presentation.res.Res
import zed.rainxch.githubstore.core.presentation.res.announcements_category_news
import zed.rainxch.githubstore.core.presentation.res.announcements_category_privacy
import zed.rainxch.githubstore.core.presentation.res.announcements_category_security
import zed.rainxch.githubstore.core.presentation.res.announcements_category_status
import zed.rainxch.githubstore.core.presentation.res.announcements_category_survey

internal fun categoryLabel(category: AnnouncementCategory): StringResource = when (category) {
    AnnouncementCategory.NEWS -> Res.string.announcements_category_news
    AnnouncementCategory.PRIVACY -> Res.string.announcements_category_privacy
    AnnouncementCategory.SURVEY -> Res.string.announcements_category_survey
    AnnouncementCategory.SECURITY -> Res.string.announcements_category_security
    AnnouncementCategory.STATUS -> Res.string.announcements_category_status
}
