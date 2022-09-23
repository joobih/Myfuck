package com.topjohnwu.myfuck.ui.home

import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.topjohnwu.myfuck.BuildConfig
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.*
import com.topjohnwu.myfuck.core.Config
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.download.Subject
import com.topjohnwu.myfuck.core.download.Subject.Manager
import com.topjohnwu.myfuck.data.repository.NetworkService
import com.topjohnwu.myfuck.events.OpenInappLinkEvent
import com.topjohnwu.myfuck.events.SnackbarEvent
import com.topjohnwu.myfuck.events.dialog.EnvFixDialog
import com.topjohnwu.myfuck.events.dialog.ManagerInstallDialog
import com.topjohnwu.myfuck.events.dialog.UninstallDialog
import com.topjohnwu.myfuck.ktx.await
import com.topjohnwu.myfuck.utils.asText
import com.topjohnwu.myfuck.utils.set
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.BR
import kotlin.math.roundToInt

enum class MyfuckState {
    NOT_INSTALLED, UP_TO_DATE, OBSOLETE, LOADING
}

class HomeViewModel(
    private val svc: NetworkService
) : BaseViewModel() {

    val myfuckTitleBarrierIds =
        intArrayOf(R.id.home_myfuck_icon, R.id.home_myfuck_title, R.id.home_myfuck_button)
    val myfuckDetailBarrierIds =
        intArrayOf(R.id.home_myfuck_installed_version, R.id.home_device_details_ramdisk)
    val appTitleBarrierIds =
        intArrayOf(R.id.home_manager_icon, R.id.home_manager_title, R.id.home_manager_button)

    @get:Bindable
    var isNoticeVisible = Config.safetyNotice
        set(value) = set(value, field, { field = it }, BR.noticeVisible)

    val stateMyfuck = when {
        !Info.env.isActive -> MyfuckState.NOT_INSTALLED
        Info.env.myfuckVersionCode < BuildConfig.VERSION_CODE -> MyfuckState.OBSOLETE
        else -> MyfuckState.UP_TO_DATE
    }

    @get:Bindable
    var stateManager = MyfuckState.LOADING
        set(value) = set(value, field, { field = it }, BR.stateManager)

    val myfuckInstalledVersion get() = Info.env.run {
        if (isActive)
            "$myfuckVersionString ($myfuckVersionCode)".asText()
        else
            R.string.not_available.asText()
    }

    @get:Bindable
    var managerRemoteVersion = R.string.loading.asText()
        set(value) = set(value, field, { field = it }, BR.managerRemoteVersion)

    val managerInstalledVersion = Info.stub?.let {
        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) (${it.version})"
    } ?: "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

    @get:Bindable
    var stateManagerProgress = 0
        set(value) = set(value, field, { field = it }, BR.stateManagerProgress)

    @get:Bindable
    val showSafetyNet get() = Info.hasGMS && isConnected.get()

    val itemBinding = itemBindingOf<IconLink> {
        it.bindExtra(BR.viewModel, this)
    }

    private var shownDialog = false

    override fun refresh() = viewModelScope.launch {
        state = State.LOADING
        notifyPropertyChanged(BR.showSafetyNet)
        Info.getRemote(svc)?.apply {
            state = State.LOADED

            stateManager = when {
                BuildConfig.VERSION_CODE < myfuck.versionCode -> MyfuckState.OBSOLETE
                else -> MyfuckState.UP_TO_DATE
            }

            managerRemoteVersion =
                "${myfuck.version} (${myfuck.versionCode}) (${stub.versionCode})".asText()

            launch {
                ensureEnv()
            }
        } ?: {
            state = State.LOADING_FAILED
            managerRemoteVersion = R.string.not_available.asText()
        }()
    }

    val showTest = false

    fun onTestPressed() = object : ViewEvent(), ActivityExecutor {
        override fun invoke(activity: BaseUIActivity<*, *>) {
            /* Entry point to trigger test events within the app */
        }
    }.publish()

    fun onProgressUpdate(progress: Float, subject: Subject) {
        if (subject is Manager)
            stateManagerProgress = progress.times(100f).roundToInt()
    }

    fun onLinkPressed(link: String) = OpenInappLinkEvent(link).publish()

    fun onDeletePressed() = UninstallDialog().publish()

    fun onManagerPressed() = when (state) {
        State.LOADED -> withExternalRW { ManagerInstallDialog().publish() }
        State.LOADING -> SnackbarEvent(R.string.loading).publish()
        else -> SnackbarEvent(R.string.no_connection).publish()
    }

    fun onMyfuckPressed() = withExternalRW {
        HomeFragmentDirections.actionHomeFragmentToInstallFragment().navigate()
    }

    fun onSafetyNetPressed() =
        HomeFragmentDirections.actionHomeFragmentToSafetynetFragment().navigate()

    fun hideNotice() {
        Config.safetyNotice = false
        isNoticeVisible = false
    }

    private suspend fun ensureEnv() {
        val invalidStates = listOf(
            MyfuckState.NOT_INSTALLED,
            MyfuckState.LOADING
        )
        if (invalidStates.any { it == stateMyfuck } || shownDialog) return

        val result = Shell.su("env_check").await()
        if (!result.isSuccess) {
            shownDialog = true
            EnvFixDialog().publish()
        }
    }

}
