package zed.rainxch.profile.presentation.components.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zed.rainxch.profile.presentation.ProfileAction
import zed.rainxch.profile.presentation.ProfileState

fun LazyListScope.settings(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    appearanceSection(
        state = state,
        onAction = onAction,
    )

    item {
        Spacer(Modifier.height(32.dp))
    }

    networkSection(
        state = state,
        onAction = onAction,
    )

    item {
        Spacer(Modifier.height(12.dp))
    }

    installationSection(
        state = state,
        onAction = onAction,
    )

    updatesSection(
        state = state,
        onAction = onAction,
    )
}
