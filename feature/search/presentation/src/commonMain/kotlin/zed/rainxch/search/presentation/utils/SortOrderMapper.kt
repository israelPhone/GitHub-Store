package zed.rainxch.search.presentation.utils

import org.jetbrains.compose.resources.StringResource
import zed.rainxch.domain.model.SortOrder
import zed.rainxch.domain.model.SortOrder.Ascending
import zed.rainxch.domain.model.SortOrder.Descending
import zed.rainxch.githubstore.core.presentation.res.Res
import zed.rainxch.githubstore.core.presentation.res.*

fun SortOrder.label(): StringResource = when (this) {
    Descending -> Res.string.sort_order_descending
    Ascending -> Res.string.sort_order_ascending
}
