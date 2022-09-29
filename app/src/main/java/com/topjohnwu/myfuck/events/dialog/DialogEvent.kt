package com.topjohnwu.myfuck.events.dialog

import com.topjohnwu.myfuck.arch.ActivityExecutor
import com.topjohnwu.myfuck.arch.UIActivity
import com.topjohnwu.myfuck.arch.ViewEvent
import com.topjohnwu.myfuck.view.MyfuckDialog

abstract class DialogEvent : ViewEvent(), ActivityExecutor {

    override fun invoke(activity: UIActivity<*>) {
        MyfuckDialog(activity)
            .apply { setOwnerActivity(activity) }
            .apply(this::build).show()
    }

    abstract fun build(dialog: MyfuckDialog)

}

typealias GenericDialogListener = () -> Unit
