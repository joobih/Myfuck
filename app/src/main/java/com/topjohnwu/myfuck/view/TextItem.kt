package com.topjohnwu.myfuck.view

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.databinding.ComparableRvItem

class TextItem(val text: Int) : ComparableRvItem<TextItem>() {
    override val layoutRes = R.layout.item_text

    override fun contentSameAs(other: TextItem) = text == other.text
    override fun itemSameAs(other: TextItem) = contentSameAs(other)
}
