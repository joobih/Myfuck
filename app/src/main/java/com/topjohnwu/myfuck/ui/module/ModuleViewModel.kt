package com.topjohnwu.myfuck.ui.module

import android.net.Uri
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import com.topjohnwu.myfuck.BR
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.AsyncLoadViewModel
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.base.ContentResultCallback
import com.topjohnwu.myfuck.core.model.module.LocalModule
import com.topjohnwu.myfuck.core.model.module.OnlineModule
import com.topjohnwu.myfuck.databinding.*
import com.topjohnwu.myfuck.events.GetContentEvent
import com.topjohnwu.myfuck.events.SnackbarEvent
import com.topjohnwu.myfuck.events.dialog.ModuleInstallDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

class ModuleViewModel : AsyncLoadViewModel() {

    val bottomBarBarrierIds = intArrayOf(R.id.module_update, R.id.module_remove)

    private val itemsInstalled = diffListOf<LocalModuleRvItem>()

    val items = MergeObservableList<RvItem>()
    val extraBindings = bindExtra {
        it.put(BR.viewModel, this)
    }

    val data get() = uri

    @get:Bindable
    var loading = true
        private set(value) = set(value, field, { field = it }, BR.loading)

    init {
        if (Info.env.isActive && LocalModule.loaded()) {
            items.insertItem(InstallModule)
                .insertList(itemsInstalled)
        }
    }

    override suspend fun doLoadWork() {
        loading = true
        loadInstalled()
        loading = false
        loadUpdateInfo()
    }

    override fun onNetworkChanged(network: Boolean) = startLoading()

    private suspend fun loadInstalled() {
        val installed = LocalModule.installed().map { LocalModuleRvItem(it) }
        val diff = withContext(Dispatchers.Default) {
            itemsInstalled.calculateDiff(installed)
        }
        itemsInstalled.update(installed, diff)
    }

    private suspend fun loadUpdateInfo() {
        withContext(Dispatchers.IO) {
            itemsInstalled.forEach {
                if (it.item.fetch())
                    it.fetchedUpdateInfo()
            }
        }
    }

    fun downloadPressed(item: OnlineModule?) =
        if (item != null && Info.isConnected.value == true) {
            withExternalRW { ModuleInstallDialog(item).publish() }
        } else {
            SnackbarEvent(R.string.no_connection).publish()
        }

    fun installPressed() = withExternalRW {
        GetContentEvent("application/zip", UriCallback()).publish()
    }

    @Parcelize
    class UriCallback : ContentResultCallback {
        override fun onActivityResult(result: Uri) {
            uri.value = result
        }
    }

    companion object {
        private val uri = MutableLiveData<Uri?>()
    }
}
