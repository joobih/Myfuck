package com.topjohnwu.myfuck.ui

import android.Manifest.permission.REQUEST_INSTALL_PACKAGES
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.topjohnwu.myfuck.BuildConfig.APPLICATION_ID
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.StubApk
import com.topjohnwu.myfuck.arch.NavigationActivity
import com.topjohnwu.myfuck.core.Config
import com.topjohnwu.myfuck.core.Const
import com.topjohnwu.myfuck.core.JobService
import com.topjohnwu.myfuck.core.di.ServiceLocator
import com.topjohnwu.myfuck.core.isRunningAsStub
import com.topjohnwu.myfuck.core.tasks.HideAPK
import com.topjohnwu.myfuck.core.utils.RootUtils
import com.topjohnwu.myfuck.ui.theme.Theme
import com.topjohnwu.myfuck.utils.Utils
import com.topjohnwu.myfuck.view.MyfuckDialog
import com.topjohnwu.myfuck.view.Notifications
import com.topjohnwu.myfuck.view.Shortcuts
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
abstract class SplashActivity<Binding : ViewDataBinding> : NavigationActivity<Binding>() {

    companion object {
        private var skipSplash = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.selected.themeRes)

        if (isRunningAsStub && !skipSplash) {
            // Manually apply splash theme for stub
            theme.applyStyle(R.style.StubSplashTheme, true)
        }

        super.onCreate(savedInstanceState)

        if (!isRunningAsStub) {
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { !skipSplash }
        }

        if (skipSplash) {
            showMainUI(savedInstanceState)
        } else {
            Shell.getShell(Shell.EXECUTOR) {
                if (isRunningAsStub && !it.isRoot) {
                    showInvalidStateMessage()
                    return@getShell
                }
                preLoad(savedInstanceState)
            }
        }
    }

    abstract fun showMainUI(savedInstanceState: Bundle?)

    @SuppressLint("InlinedApi")
    private fun showInvalidStateMessage(): Unit = runOnUiThread {
        MyfuckDialog(this).apply {
            setTitle(R.string.unsupport_nonroot_stub_title)
            setMessage(R.string.unsupport_nonroot_stub_msg)
            setButton(MyfuckDialog.ButtonType.POSITIVE) {
                text = R.string.install
                onClick {
                    withPermission(REQUEST_INSTALL_PACKAGES) {
                        if (!it) {
                            Utils.toast(R.string.install_unknown_denied, Toast.LENGTH_SHORT)
                            showInvalidStateMessage()
                        } else {
                            lifecycleScope.launch {
                                HideAPK.restore(this@SplashActivity)
                            }
                        }
                    }
                }
            }
            setCancelable(false)
            show()
        }
    }

    private fun preLoad(savedState: Bundle?) {
        val prevPkg = intent.getStringExtra(Const.Key.PREV_PKG)?.let {
            // Make sure the calling package matches (prevent DoS)
            if (it == realCallingPackage)
                it
            else
                null
        }

        Config.load(prevPkg)
        handleRepackage(prevPkg)
        if (prevPkg != null) {
            runOnUiThread {
                // Relaunch the process after package migration
                StubApk.restartProcess(this)
            }
            return
        }

        Notifications.setup(this)
        JobService.schedule(this)
        Shortcuts.setupDynamic(this)

        // Pre-fetch network services
        ServiceLocator.networkService

        // Wait for root service
        RootUtils.Connection.await()

        runOnUiThread {
            skipSplash = true
            if (isRunningAsStub) {
                // Re-launch main activity without splash theme
                relaunch()
            } else {
                showMainUI(savedState)
            }
        }
    }

    private fun handleRepackage(pkg: String?) {
        if (packageName != APPLICATION_ID) {
            runCatching {
                // Hidden, remove com.topjohnwu.myfuck if exist as it could be malware
                packageManager.getApplicationInfo(APPLICATION_ID, 0)
                Shell.cmd("(pm uninstall $APPLICATION_ID)& >/dev/null 2>&1").exec()
            }
        } else {
            if (!Const.Version.atLeast_25_0() && Config.suManager.isNotEmpty())
                Config.suManager = ""
            pkg ?: return
            Shell.cmd("(pm uninstall $pkg)& >/dev/null 2>&1").exec()
        }
    }

}
