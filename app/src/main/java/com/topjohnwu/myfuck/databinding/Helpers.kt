package com.topjohnwu.myfuck.databinding

fun <T : AnyDiffRvItem> diffListOf() =
    DiffObservableList(DiffRvItem.callback<T>())

fun <T : AnyDiffRvItem> filterableListOf() =
    FilterableDiffObservableList(DiffRvItem.callback<T>())
