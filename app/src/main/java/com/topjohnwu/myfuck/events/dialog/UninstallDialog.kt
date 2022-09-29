package com.topjohnwu.myfuck.events.dialog

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.NavigationActivity
import com.topjohnwu.myfuck.ui.flash.FlashFragment
import com.topjohnwu.myfuck.utils.Utils
import com.topjohnwu.myfuck.view.MyfuckDialog
import com.topjohnwu.superuser.Shell

class UninstallDialog : DialogEvent() {

    override fun build(dialog: MyfuckDialog) {
        dialog.apply {
            setTitle(R.string.uninstall_myfuck_title)
            setMessage(R.string.uninstall_myfuck_msg)
            setButton(MyfuckDialog.ButtonType.POSITIVE) {
                text = R.string.restore_img
                onClick { restore(dialog.context) }
            }
            setButton(MyfuckDialog.ButtonType.NEGATIVE) {
                text = R.string.complete_uninstall
                onClick { completeUninstall(dialog) }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun restore(context: Context) {
        val dialog = ProgressDialog(context).apply {
            setMessage(context.getString(R.string.restore_img_msg))
            show()
        }

        Shell.cmd("restore_imgs").submit { result ->
            dialog.dismiss()
            if (result.isSuccess) {
                Utils.toast(R.string.restore_done, Toast.LENGTH_SHORT)
            } else {
                Utils.toast(R.string.restore_fail, Toast.LENGTH_LONG)
            }
        }
    }

    private fun completeUninstall(dialog: MyfuckDialog) {
        (dialog.ownerActivity as NavigationActivity<*>)
            .navigation.navigate(FlashFragment.uninstall())
    }

}
