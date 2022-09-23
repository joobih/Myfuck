package com.topjohnwu.myfuck.events.dialog

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.view.MyfuckDialog

class SuperuserRevokeDialog(
    builder: Builder.() -> Unit
) : DialogEvent() {

    private val callbacks = Builder().apply(builder)

    override fun build(dialog: MyfuckDialog) {
        dialog.applyTitle(R.string.su_revoke_title)
            .applyMessage(R.string.su_revoke_msg, callbacks.appName)
            .applyButton(MyfuckDialog.ButtonType.POSITIVE) {
                titleRes = android.R.string.ok
                onClick { callbacks.listenerOnSuccess() }
            }
            .applyButton(MyfuckDialog.ButtonType.NEGATIVE) {
                titleRes = android.R.string.cancel
            }
    }

    inner class Builder internal constructor() {
        var appName: String = ""

        internal var listenerOnSuccess: GenericDialogListener = {}

        fun onSuccess(listener: GenericDialogListener) {
            listenerOnSuccess = listener
        }
    }
}
