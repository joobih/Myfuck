package com.topjohnwu.myfuck.ui.flash

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.navigation.NavDeepLinkBuilder
import com.topjohnwu.myfuck.MainDirections
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.BaseFragment
import com.topjohnwu.myfuck.arch.viewModel
import com.topjohnwu.myfuck.core.Const
import com.topjohnwu.myfuck.core.cmp
import com.topjohnwu.myfuck.databinding.FragmentFlashMd2Binding
import com.topjohnwu.myfuck.ui.MainActivity

class FlashFragment : BaseFragment<FragmentFlashMd2Binding>() {

    override val layoutRes = R.layout.fragment_flash_md2
    override val viewModel by viewModel<FlashViewModel>()
    override val snackbarView: View get() = binding.snackbarContainer
    override val snackbarAnchorView: View?
        get() = if (binding.restartBtn.isShown) binding.restartBtn else super.snackbarAnchorView

    private var defaultOrientation = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.args = FlashFragmentArgs.fromBundle(requireArguments())
    }

    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
        activity?.setTitle(R.string.flash_screen_title)

        viewModel.state.observe(this) {
            activity?.supportActionBar?.setSubtitle(
                when (it) {
                    FlashViewModel.State.FLASHING -> R.string.flashing
                    FlashViewModel.State.SUCCESS -> R.string.done
                    FlashViewModel.State.FAILED -> R.string.failure
                }
            )
            if (it == FlashViewModel.State.SUCCESS && viewModel.showReboot) {
                binding.restartBtn.apply {
                    if (!this.isVisible) this.show()
                    if (!this.isFocused) this.requestFocus()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_flash, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return viewModel.onMenuItemClicked(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultOrientation = activity?.requestedOrientation ?: -1
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        if (savedInstanceState == null) {
            viewModel.startFlashing()
        }
    }

    @SuppressLint("WrongConstant")
    override fun onDestroyView() {
        if (defaultOrientation != -1) {
            activity?.requestedOrientation = defaultOrientation
        }
        super.onDestroyView()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> true
            else -> false
        }
    }

    override fun onBackPressed(): Boolean {
        if (viewModel.flashing.value == true)
            return true
        return super.onBackPressed()
    }

    override fun onPreBind(binding: FragmentFlashMd2Binding) = Unit

    companion object {

        private fun createIntent(context: Context, args: FlashFragmentArgs) =
            NavDeepLinkBuilder(context)
                .setGraph(R.navigation.main)
                .setComponentName(MainActivity::class.java.cmp(context.packageName))
                .setDestination(R.id.flashFragment)
                .setArguments(args.toBundle())
                .createPendingIntent()

        private fun flashType(isSecondSlot: Boolean) =
            if (isSecondSlot) Const.Value.FLASH_INACTIVE_SLOT else Const.Value.FLASH_MYFUCK

        /* Flashing is understood as installing / flashing myfuck itself */

        fun flash(isSecondSlot: Boolean) = MainDirections.actionFlashFragment(
            action = flashType(isSecondSlot)
        )

        /* Patching is understood as injecting img files with myfuck */

        fun patch(uri: Uri) = MainDirections.actionFlashFragment(
            action = Const.Value.PATCH_FILE,
            additionalData = uri
        )

        /* Uninstalling is understood as removing myfuck entirely */

        fun uninstall() = MainDirections.actionFlashFragment(
            action = Const.Value.UNINSTALL
        )

        /* Installing is understood as flashing modules / zips */

        fun installIntent(context: Context, file: Uri) = FlashFragmentArgs(
            action = Const.Value.FLASH_ZIP,
            additionalData = file,
        ).let { createIntent(context, it) }

        fun install(file: Uri) = MainDirections.actionFlashFragment(
            action = Const.Value.FLASH_ZIP,
            additionalData = file,
        )
    }

}
