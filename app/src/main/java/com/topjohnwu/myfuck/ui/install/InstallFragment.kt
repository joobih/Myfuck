package com.topjohnwu.myfuck.ui.install

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.BaseFragment
import com.topjohnwu.myfuck.arch.viewModel
import com.topjohnwu.myfuck.databinding.FragmentInstallMd2Binding

class InstallFragment : BaseFragment<FragmentInstallMd2Binding>() {

    override val layoutRes = R.layout.fragment_install_md2
    override val viewModel by viewModel<InstallViewModel>()

    override fun onStart() {
        super.onStart()
        requireActivity().setTitle(R.string.install)
    }
}
