package com.topjohnwu.myfuck.ui.theme

import com.topjohnwu.myfuck.arch.BaseViewModel
import com.topjohnwu.myfuck.core.Config
import com.topjohnwu.myfuck.events.RecreateEvent
import com.topjohnwu.myfuck.events.dialog.DarkThemeDialog
import com.topjohnwu.myfuck.view.TappableHeadlineItem

class ThemeViewModel : BaseViewModel(), TappableHeadlineItem.Listener {

    val themeHeadline = TappableHeadlineItem.ThemeMode

    override fun onItemPressed(item: TappableHeadlineItem) = when (item) {
        is TappableHeadlineItem.ThemeMode -> DarkThemeDialog().publish()
    }

    fun saveTheme(theme: Theme) {
        if (!theme.isSelected) {
            Config.themeOrdinal = theme.ordinal
            RecreateEvent().publish()
        }
    }
}
