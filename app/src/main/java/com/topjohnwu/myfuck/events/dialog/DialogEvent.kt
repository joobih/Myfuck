package com.topjohnwu.myfuck.events.dialog

import com.topjohnwu.myfuck.arch.ActivityExecutor
import com.topjohnwu.myfuck.arch.BaseUIActivity
import com.topjohnwu.myfuck.arch.ViewEvent
import com.topjohnwu.myfuck.view.MyfuckDialog

abstract class DialogEvent : ViewEvent(), ActivityExecutor {

    protected lateinit var dialog: MyfuckDialog

    override fun invoke(activity: BaseUIActivity<*, *>) {
        dialog = MyfuckDialog(activity)
            .apply { setOwnerActivity(activity) }
            .apply(this::build).reveal()
    }

    abstract fun build(dialog: MyfuckDialog)

}

typealias GenericDialogListener = () -> Unit
