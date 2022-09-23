package com.topjohnwu.myfuck.events

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.myfuck.arch.ActivityExecutor
import com.topjohnwu.myfuck.arch.BaseUIActivity
import com.topjohnwu.myfuck.arch.ViewEvent
import com.topjohnwu.myfuck.utils.TextHolder
import com.topjohnwu.myfuck.utils.asText

class SnackbarEvent constructor(
    private val msg: TextHolder,
    private val length: Int = Snackbar.LENGTH_SHORT,
    private val builder: Snackbar.() -> Unit = {}
) : ViewEvent(), ActivityExecutor {

    constructor(
        @StringRes res: Int,
        length: Int = Snackbar.LENGTH_SHORT,
        builder: Snackbar.() -> Unit = {}
    ) : this(res.asText(), length, builder)

    constructor(
        msg: String,
        length: Int = Snackbar.LENGTH_SHORT,
        builder: Snackbar.() -> Unit = {}
    ) : this(msg.asText(), length, builder)


    private fun snackbar(
        view: View,
        message: String,
        length: Int,
        builder: Snackbar.() -> Unit
    ) = Snackbar.make(view, message, length).apply(builder).show()

    override fun invoke(activity: BaseUIActivity<*, *>) {
        snackbar(activity.snackbarView,
            msg.getText(activity.resources).toString(),
            length, builder)
    }
}
