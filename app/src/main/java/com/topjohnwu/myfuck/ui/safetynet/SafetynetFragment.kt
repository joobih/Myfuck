package com.topjohnwu.myfuck.ui.safetynet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.BaseUIFragment
import com.topjohnwu.myfuck.databinding.FragmentSafetynetMd2Binding
import com.topjohnwu.myfuck.di.viewModel

class SafetynetFragment : BaseUIFragment<SafetynetViewModel, FragmentSafetynetMd2Binding>() {

    override val layoutRes = R.layout.fragment_safetynet_md2
    override val viewModel by viewModel<SafetynetViewModel>()

    override fun onStart() {
        super.onStart()
        activity.setTitle(R.string.safetynet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        // Set barrier reference IDs in code, since resource IDs will be stripped in release mode
        binding.snetBarrier.referencedIds = intArrayOf(R.id.basic_text, R.id.cts_text)

        return binding.root
    }

}
