package com.topjohnwu.myfuck.events.dialog

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.view.MyfuckDialog

class SecondSlotWarningDialog : DialogEvent() {

    override fun build(dialog: MyfuckDialog) {
        dialog.applyTitle(android.R.string.dialog_alert_title)
            .applyMessage(R.string.install_inactive_slot_msg)
            .applyButton(MyfuckDialog.ButtonType.POSITIVE) {
                titleRes = android.R.string.ok
            }
            .cancellable(true)
    }
}
