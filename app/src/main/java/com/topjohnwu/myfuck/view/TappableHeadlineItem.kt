package com.topjohnwu.myfuck.view

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.databinding.DiffRvItem

sealed class TappableHeadlineItem : DiffRvItem<TappableHeadlineItem>() {

    abstract val title: Int
    abstract val icon: Int

    override val layoutRes = R.layout.item_tappable_headline

    // --- listener

    interface Listener {

        fun onItemPressed(item: TappableHeadlineItem)

    }

    // --- objects

    object ThemeMode : TappableHeadlineItem() {
        override val title = R.string.settings_dark_mode_title
        override val icon = R.drawable.ic_day_night
    }

}
