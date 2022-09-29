package com.topjohnwu.myfuck.ui.home

import android.content.Context
import androidx.core.net.toUri
import androidx.databinding.Bindable
import com.topjohnwu.myfuck.BR
import com.topjohnwu.myfuck.BuildConfig
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.*
import com.topjohnwu.myfuck.core.Config
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.download.Subject
import com.topjohnwu.myfuck.core.download.Subject.App
import com.topjohnwu.myfuck.core.repository.NetworkService
import com.topjohnwu.myfuck.databinding.bindExtra
import com.topjohnwu.myfuck.databinding.set
import com.topjohnwu.myfuck.events.SnackbarEvent
import com.topjohnwu.myfuck.events.dialog.EnvFixDialog
import com.topjohnwu.myfuck.events.dialog.ManagerInstallDialog
import com.topjohnwu.myfuck.events.dialog.UninstallDialog
import com.topjohnwu.myfuck.ktx.await
import com.topjohnwu.myfuck.utils.Utils
import com.topjohnwu.myfuck.utils.asText
import com.topjohnwu.superuser.Shell
import kotlin.math.roundToInt

class HomeViewModel(
    private val svc: NetworkService
) : AsyncLoadViewModel() {

    enum class State {
        LOADING, INVALID, OUTDATED, UP_TO_DATE
    }

    val myfuckTitleBarrierIds =
        intArrayOf(R.id.home_myfuck_icon, R.id.home_myfuck_title, R.id.home_myfuck_button)
    val appTitleBarrierIds =
        intArrayOf(R.id.home_manager_icon, R.id.home_manager_title, R.id.home_manager_button)

    @get:Bindable
    var isNoticeVisible = Config.safetyNotice
        set(value) = set(value, field, { field = it }, BR.noticeVisible)

    val myfuckState
        get() = when {
            !Info.env.isActive -> State.INVALID
            Info.env.versionCode < BuildConfig.VERSION_CODE -> State.OUTDATED
            else -> State.UP_TO_DATE
        }

    @get:Bindable
    var appState = State.LOADING
        set(value) = set(value, field, { field = it }, BR.appState)

    val myfuckInstalledVersion
        get() = Info.env.run {
            if (isActive)
                ("$versionString ($versionCode)" + if (isDebug) " (D)" else "").asText()
            else
                R.string.not_available.asText()
        }

    @get:Bindable
    var managerRemoteVersion = R.string.loading.asText()
        set(value) = set(value, field, { field = it }, BR.managerRemoteVersion)

    val managerInstalledVersion
        get() = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})" +
            Info.stub?.let { " (${it.version})" }.orEmpty() +
            if (BuildConfig.DEBUG) " (D)" else ""

    @get:Bindable
    var stateManagerProgress = 0
        set(value) = set(value, field, { field = it }, BR.stateManagerProgress)

    val extraBindings = bindExtra {
        it.put(BR.viewModel, this)
    }

    companion object {
        private var checkedEnv = false
    }

    override suspend fun doLoadWork() {
        appState = State.LOADING
        Info.getRemote(svc)?.apply {
            appState = when {
                BuildConfig.VERSION_CODE < myfuck.versionCode -> State.OUTDATED
                else -> State.UP_TO_DATE
            }

            val isDebug = Config.updateChannel == Config.Value.DEBUG_CHANNEL
            managerRemoteVersion =
                ("${myfuck.version} (${myfuck.versionCode}) (${stub.versionCode})" +
                    if (isDebug) " (D)" else "").asText()
        } ?: run {
            appState = State.INVALID
            managerRemoteVersion = R.string.not_available.asText()
        }
        ensureEnv()
    }

    override fun onNetworkChanged(network: Boolean) = startLoading()

    fun onProgressUpdate(progress: Float, subject: Subject) {
        if (subject is App)
            stateManagerProgress = progress.times(100f).roundToInt()
    }

    fun onLinkPressed(link: String) = object : ViewEvent(), ContextExecutor {
        override fun invoke(context: Context) = Utils.openLink(context, link.toUri())
    }.publish()

    fun onDeletePressed() = UninstallDialog().publish()

    fun onManagerPressed() = when (appState) {
        State.LOADING -> SnackbarEvent(R.string.loading).publish()
        State.INVALID -> SnackbarEvent(R.string.no_connection).publish()
        else -> withExternalRW {
            withInstallPermission {
                ManagerInstallDialog().publish()
            }
        }
    }

    fun onMyfuckPressed() = withExternalRW {
        HomeFragmentDirections.actionHomeFragmentToInstallFragment().navigate()
    }

    fun hideNotice() {
        Config.safetyNotice = false
        isNoticeVisible = false
    }

    private suspend fun ensureEnv() {
        if (myfuckState == State.INVALID || checkedEnv) return
        val cmd = "env_check ${Info.env.versionString} ${Info.env.versionCode}"
        if (!Shell.cmd(cmd).await().isSuccess) {
            EnvFixDialog(this).publish()
        }
        checkedEnv = true
    }

    val showTest = false
    fun onTestPressed() = object : ViewEvent(), ActivityExecutor {
        override fun invoke(activity: UIActivity<*>) {
            /* Entry point to trigger test events within the app */
        }
    }.publish()
}
