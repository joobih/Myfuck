package com.topjohnwu.myfuck.events.dialog

import com.topjohnwu.myfuck.arch.ActivityExecutor
import com.topjohnwu.myfuck.arch.BaseUIActivity
import com.topjohnwu.myfuck.arch.ViewEvent
import com.topjohnwu.myfuck.core.utils.BiometricHelper

class BiometricEvent(
    builder: Builder.() -> Unit
) : ViewEvent(), ActivityExecutor {

    private var listenerOnFailure: GenericDialogListener = {}
    private var listenerOnSuccess: GenericDialogListener = {}

    init {
        builder(Builder())
    }

    override fun invoke(activity: BaseUIActivity<*, *>) {
        BiometricHelper.authenticate(
            activity,
            onError = listenerOnFailure,
            onSuccess = listenerOnSuccess
        )
    }

    inner class Builder internal constructor() {

        fun onFailure(listener: GenericDialogListener) {
            listenerOnFailure = listener
        }

        fun onSuccess(listener: GenericDialogListener) {
            listenerOnSuccess = listener
        }
    }

}
