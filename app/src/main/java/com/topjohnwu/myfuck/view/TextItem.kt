package com.topjohnwu.myfuck.view

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.databinding.DiffRvItem

class TextItem(val text: Int) : DiffRvItem<TextItem>() {
    override val layoutRes = R.layout.item_text

    override fun contentSameAs(other: TextItem) = text == other.text
}
