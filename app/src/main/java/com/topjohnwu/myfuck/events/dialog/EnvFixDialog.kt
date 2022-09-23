package com.topjohnwu.myfuck.events.dialog

import androidx.lifecycle.lifecycleScope
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.core.base.BaseActivity
import com.topjohnwu.myfuck.core.tasks.MyfuckInstaller
import com.topjohnwu.myfuck.view.MyfuckDialog
import kotlinx.coroutines.launch

class EnvFixDialog : DialogEvent() {

    override fun build(dialog: MyfuckDialog) = dialog
        .applyTitle(R.string.env_fix_title)
        .applyMessage(R.string.env_fix_msg)
        .applyButton(MyfuckDialog.ButtonType.POSITIVE) {
            titleRes = android.R.string.ok
            preventDismiss = true
            onClick {
                dialog.applyTitle(R.string.setup_title)
                    .applyMessage(R.string.setup_msg)
                    .resetButtons()
                    .cancellable(false)
                (dialog.ownerActivity as BaseActivity).lifecycleScope.launch {
                    MyfuckInstaller.FixEnv {
                        dialog.dismiss()
                    }.exec()
                }
            }
        }
        .applyButton(MyfuckDialog.ButtonType.NEGATIVE) {
            titleRes = android.R.string.cancel
        }
        .let { }

    companion object {
        const val DISMISS = "com.topjohnwu.myfuck.ENV_DONE"
    }
}
