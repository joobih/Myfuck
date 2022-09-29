package com.topjohnwu.myfuck.events.dialog

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.view.MyfuckDialog

class SuperuserRevokeDialog(
    builder: Builder.() -> Unit
) : DialogEvent() {

    private val callbacks = Builder().apply(builder)

    override fun build(dialog: MyfuckDialog) {
        dialog.apply {
            setTitle(R.string.su_revoke_title)
            setMessage(R.string.su_revoke_msg, callbacks.appName)
            setButton(MyfuckDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                onClick { callbacks.listenerOnSuccess() }
            }
            setButton(MyfuckDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
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
