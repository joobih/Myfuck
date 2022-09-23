package com.topjohnwu.myfuck.events.dialog

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.core.Config
import com.topjohnwu.myfuck.view.MyfuckDialog

class DarkThemeDialog : DialogEvent() {

    override fun build(dialog: MyfuckDialog) {
        val activity = dialog.context.unwrap()
        dialog.applyTitle(R.string.settings_dark_mode_title)
            .applyMessage(R.string.settings_dark_mode_message)
            .applyButton(MyfuckDialog.ButtonType.POSITIVE) {
                titleRes = R.string.settings_dark_mode_light
                icon = R.drawable.ic_day
                onClick { selectTheme(AppCompatDelegate.MODE_NIGHT_NO, activity) }
            }
            .applyButton(MyfuckDialog.ButtonType.NEUTRAL) {
                titleRes = R.string.settings_dark_mode_system
                icon = R.drawable.ic_day_night
                onClick { selectTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, activity) }
            }
            .applyButton(MyfuckDialog.ButtonType.NEGATIVE) {
                titleRes = R.string.settings_dark_mode_dark
                icon = R.drawable.ic_night
                onClick { selectTheme(AppCompatDelegate.MODE_NIGHT_YES, activity) }
            }
    }

    private fun Context.unwrap(): Activity {
        return when(this) {
            is Activity -> this
            is ContextWrapper -> baseContext.unwrap()
            else -> error("Cannot happen")
        }
    }

    private fun selectTheme(mode: Int, activity: Activity) {
        Config.darkTheme = mode
        activity.recreate()
    }
}
