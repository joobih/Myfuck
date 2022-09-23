package com.topjohnwu.myfuck.ui.flash

import android.view.MenuItem
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.topjohnwu.myfuck.BR
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.BaseViewModel
import com.topjohnwu.myfuck.arch.diffListOf
import com.topjohnwu.myfuck.arch.itemBindingOf
import com.topjohnwu.myfuck.core.Const
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.tasks.FlashZip
import com.topjohnwu.myfuck.core.tasks.MyfuckInstaller
import com.topjohnwu.myfuck.core.utils.MediaStoreUtils
import com.topjohnwu.myfuck.core.utils.MediaStoreUtils.outputStream
import com.topjohnwu.myfuck.databinding.RvBindingAdapter
import com.topjohnwu.myfuck.events.SnackbarEvent
import com.topjohnwu.myfuck.ktx.*
import com.topjohnwu.myfuck.utils.set
import com.topjohnwu.myfuck.view.Notifications
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FlashViewModel : BaseViewModel() {

    @get:Bindable
    var showReboot = Shell.rootAccess()
        set(value) = set(value, field, { field = it }, BR.showReboot)

    private val _subtitle = MutableLiveData(R.string.flashing)
    val subtitle get() = _subtitle as LiveData<Int>

    val adapter = RvBindingAdapter<ConsoleItem>()
    val items = diffListOf<ConsoleItem>()
    val itemBinding = itemBindingOf<ConsoleItem>()
    lateinit var args: FlashFragmentArgs

    private val logItems = mutableListOf<String>().synchronized()
    private val outItems = object : CallbackList<String>() {
        override fun onAddElement(e: String?) {
            e ?: return
            items.add(ConsoleItem(e))
            logItems.add(e)
        }
    }

    fun startFlashing() {
        val (action, uri, id) = args
        if (id != -1)
            Notifications.mgr.cancel(id)

        viewModelScope.launch {
            val result = when (action) {
                Const.Value.FLASH_ZIP -> {
                    FlashZip(uri!!, outItems, logItems).exec()
                }
                Const.Value.UNINSTALL -> {
                    showReboot = false
                    MyfuckInstaller.Uninstall(outItems, logItems).exec()
                }
                Const.Value.FLASH_MYFUCK -> {
                    if (Info.isEmulator)
                        MyfuckInstaller.Emulator(outItems, logItems).exec()
                    else
                        MyfuckInstaller.Direct(outItems, logItems).exec()
                }
                Const.Value.FLASH_INACTIVE_SLOT -> {
                    MyfuckInstaller.SecondSlot(outItems, logItems).exec()
                }
                Const.Value.PATCH_FILE -> {
                    uri ?: return@launch
                    showReboot = false
                    MyfuckInstaller.Patch(uri, outItems, logItems).exec()
                }
                else -> {
                    back()
                    return@launch
                }
            }
            onResult(result)
        }
    }

    private fun onResult(success: Boolean) {
        state = if (success) State.LOADED else State.LOADING_FAILED
        when {
            success -> _subtitle.postValue(R.string.done)
            else -> _subtitle.postValue(R.string.failure)
        }
    }

    fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> savePressed()
        }
        return true
    }

    private fun savePressed() = withExternalRW {
        viewModelScope.launch(Dispatchers.IO) {
            val name = "myfuck_install_log_%s.log".format(now.toTime(timeFormatStandard))
            val file = MediaStoreUtils.getFile(name, true)
            file.uri.outputStream().bufferedWriter().use { writer ->
                logItems.forEach {
                    writer.write(it)
                    writer.newLine()
                }
            }
            SnackbarEvent(file.toString()).publish()
        }
    }

    fun restartPressed() = reboot()
}
