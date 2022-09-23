package com.topjohnwu.myfuck.events.dialog

import android.app.ProgressDialog
import android.widget.Toast
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.BaseUIActivity
import com.topjohnwu.myfuck.ui.flash.FlashFragment
import com.topjohnwu.myfuck.utils.Utils
import com.topjohnwu.myfuck.view.MyfuckDialog
import com.topjohnwu.superuser.Shell

class UninstallDialog : DialogEvent() {

    override fun build(dialog: MyfuckDialog) {
        dialog.applyTitle(R.string.uninstall_myfuck_title)
            .applyMessage(R.string.uninstall_myfuck_msg)
            .applyButton(MyfuckDialog.ButtonType.POSITIVE) {
                titleRes = R.string.restore_img
                onClick { restore() }
            }
            .applyButton(MyfuckDialog.ButtonType.NEGATIVE) {
                titleRes = R.string.complete_uninstall
                onClick { completeUninstall() }
            }
    }

    @Suppress("DEPRECATION")
    private fun restore() {
        val dialog = ProgressDialog(dialog.context).apply {
            setMessage(dialog.context.getString(R.string.restore_img_msg))
            show()
        }

        Shell.su("restore_imgs").submit { result ->
            dialog.dismiss()
            if (result.isSuccess) {
                Utils.toast(R.string.restore_done, Toast.LENGTH_SHORT)
            } else {
                Utils.toast(R.string.restore_fail, Toast.LENGTH_LONG)
            }
        }
    }

    private fun completeUninstall() {
        (dialog.ownerActivity as? BaseUIActivity<*, *>)
                ?.navigation?.navigate(FlashFragment.uninstall())
    }

}
