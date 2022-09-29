package com.topjohnwu.myfuck.events.dialog

import androidx.lifecycle.lifecycleScope
import com.topjohnwu.myfuck.BuildConfig
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.base.BaseActivity
import com.topjohnwu.myfuck.core.tasks.MyfuckInstaller
import com.topjohnwu.myfuck.ui.home.HomeViewModel
import com.topjohnwu.myfuck.view.MyfuckDialog
import kotlinx.coroutines.launch

class EnvFixDialog(private val vm: HomeViewModel) : DialogEvent() {

    override fun build(dialog: MyfuckDialog) {
        dialog.apply {
            setTitle(R.string.env_fix_title)
            setMessage(R.string.env_fix_msg)
            setButton(MyfuckDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                doNotDismiss = true
                onClick {
                    dialog.apply {
                        setTitle(R.string.setup_title)
                        setMessage(R.string.setup_msg)
                        resetButtons()
                        setCancelable(false)
                    }
                    (dialog.ownerActivity as BaseActivity).lifecycleScope.launch {
                        MyfuckInstaller.FixEnv {
                            dialog.dismiss()
                        }.exec()
                    }
                }
            }
            setButton(MyfuckDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
        }

        if (Info.env.versionCode != BuildConfig.VERSION_CODE ||
            Info.env.versionString != BuildConfig.VERSION_NAME) {
            dialog.setButton(MyfuckDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                onClick {
                    vm.onMyfuckPressed()
                    dialog.dismiss()
                }
            }
        }
    }
}
