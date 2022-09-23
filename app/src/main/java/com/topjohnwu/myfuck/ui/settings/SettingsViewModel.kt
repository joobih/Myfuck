package com.topjohnwu.myfuck.ui.settings

import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.viewModelScope
import com.topjohnwu.myfuck.BR
import com.topjohnwu.myfuck.BuildConfig
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.BaseViewModel
import com.topjohnwu.myfuck.arch.adapterOf
import com.topjohnwu.myfuck.arch.diffListOf
import com.topjohnwu.myfuck.arch.itemBindingOf
import com.topjohnwu.myfuck.core.Const
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.isRunningAsStub
import com.topjohnwu.myfuck.core.tasks.HideAPK
import com.topjohnwu.myfuck.data.database.RepoDao
import com.topjohnwu.myfuck.di.AppContext
import com.topjohnwu.myfuck.events.AddHomeIconEvent
import com.topjohnwu.myfuck.events.RecreateEvent
import com.topjohnwu.myfuck.events.dialog.BiometricEvent
import com.topjohnwu.myfuck.ktx.activity
import com.topjohnwu.myfuck.utils.Utils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repositoryDao: RepoDao
) : BaseViewModel(), BaseSettingsItem.Callback {

    val adapter = adapterOf<BaseSettingsItem>()
    val itemBinding = itemBindingOf<BaseSettingsItem> { it.bindExtra(BR.callback, this) }
    val items = diffListOf(createItems())

    init {
        viewModelScope.launch {
            Language.loadLanguages(this)
        }
    }

    private fun createItems(): List<BaseSettingsItem> {
        val context = AppContext
        val hidden = context.packageName != BuildConfig.APPLICATION_ID

        // Customization
        val list = mutableListOf(
            Customization,
            Theme, Language
        )
        if (isRunningAsStub && ShortcutManagerCompat.isRequestPinShortcutSupported(context))
            list.add(AddShortcut)

        // Manager
        list.addAll(listOf(
            AppSettings,
            UpdateChannel, UpdateChannelUrl, DoHToggle, UpdateChecker, DownloadPath
        ))
        if (Info.env.isActive) {
            list.add(ClearRepoCache)
            if (Const.USER_ID == 0) {
                if (hidden)
                    list.add(Restore)
                else if (Info.isConnected.get())
                    list.add(Hide)
            }
        }

        // Myfuck
        if (Info.env.isActive) {
            list.addAll(listOf(
                Myfuck,
                MyfuckHide, SystemlessHosts
            ))
        }

        // Superuser
        if (Utils.showSuperUser()) {
            list.addAll(listOf(
                Superuser,
                Tapjack, Biometrics, AccessMode, MultiuserMode, MountNamespaceMode,
                AutomaticResponse, RequestTimeout, SUNotification
            ))
            if (Build.VERSION.SDK_INT < 23) {
                // Biometric is only available on 6.0+
                list.remove(Biometrics)
            }
            if (Build.VERSION.SDK_INT < 26) {
                // Re-authenticate is not feasible on 8.0+
                list.add(Reauthenticate)
            }
        }

        return list
    }

    override fun onItemPressed(view: View, item: BaseSettingsItem, callback: () -> Unit) = when (item) {
        is DownloadPath -> withExternalRW(callback)
        is Biometrics -> authenticate(callback)
        is Theme -> SettingsFragmentDirections.actionSettingsFragmentToThemeFragment().navigate()
        is ClearRepoCache -> clearRepoCache()
        is SystemlessHosts -> createHosts()
        is Restore -> HideAPK.restore(view.activity)
        is AddShortcut -> AddHomeIconEvent().publish()
        else -> callback()
    }

    override fun onItemChanged(view: View, item: BaseSettingsItem) {
        when (item) {
            is Language -> RecreateEvent().publish()
            is UpdateChannel -> openUrlIfNecessary(view)
            is Hide -> viewModelScope.launch { HideAPK.hide(view.activity, item.value) }
            else -> Unit
        }
    }

    private fun openUrlIfNecessary(view: View) {
        UpdateChannelUrl.refresh()
        if (UpdateChannelUrl.isEnabled && UpdateChannelUrl.value.isBlank()) {
            UpdateChannelUrl.onPressed(view, this)
        }
    }

    private fun authenticate(callback: () -> Unit) {
        BiometricEvent {
            // allow the change on success
            onSuccess { callback() }
        }.publish()
    }

    private fun clearRepoCache() {
        viewModelScope.launch {
            repositoryDao.clear()
            Utils.toast(R.string.repo_cache_cleared, Toast.LENGTH_SHORT)
        }
    }

    private fun createHosts() {
        Shell.su("add_hosts_module").submit {
            Utils.toast(R.string.settings_hosts_toast, Toast.LENGTH_SHORT)
        }
    }
}
