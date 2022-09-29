package com.topjohnwu.myfuck.ui.log

import androidx.databinding.Bindable
import com.topjohnwu.myfuck.BR
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.core.model.su.SuLog
import com.topjohnwu.myfuck.databinding.ObservableDiffRvItem
import com.topjohnwu.myfuck.databinding.RvContainer
import com.topjohnwu.myfuck.databinding.set
import com.topjohnwu.myfuck.ktx.timeDateFormat
import com.topjohnwu.myfuck.ktx.toTime

class LogRvItem(
    override val item: SuLog
) : ObservableDiffRvItem<LogRvItem>(), RvContainer<SuLog> {

    override val layoutRes = R.layout.item_log_access_md2

    val date = item.time.toTime(timeDateFormat)

    @get:Bindable
    var isTop = false
        set(value) = set(value, field, { field = it }, BR.top)

    @get:Bindable
    var isBottom = false
        set(value) = set(value, field, { field = it }, BR.bottom)

    override fun itemSameAs(other: LogRvItem) = item.appName == other.item.appName
}
