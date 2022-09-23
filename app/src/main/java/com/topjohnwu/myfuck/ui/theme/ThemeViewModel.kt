package com.topjohnwu.myfuck.ui.theme

import com.topjohnwu.myfuck.arch.BaseViewModel
import com.topjohnwu.myfuck.events.RecreateEvent
import com.topjohnwu.myfuck.events.dialog.DarkThemeDialog
import com.topjohnwu.myfuck.view.TappableHeadlineItem

class ThemeViewModel : BaseViewModel(), TappableHeadlineItem.Listener {

    val themeHeadline = TappableHeadlineItem.ThemeMode

    override fun onItemPressed(item: TappableHeadlineItem) = when (item) {
        is TappableHeadlineItem.ThemeMode -> darkModePressed()
        else -> Unit
    }

    fun saveTheme(theme: Theme) {
        theme.select()
        RecreateEvent().publish()
    }

    private fun darkModePressed() = DarkThemeDialog().publish()

}
