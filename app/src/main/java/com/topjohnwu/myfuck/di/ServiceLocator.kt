package com.topjohnwu.myfuck.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.room.Room
import com.topjohnwu.myfuck.core.Const
import com.topjohnwu.myfuck.core.myfuckdb.PolicyDao
import com.topjohnwu.myfuck.core.myfuckdb.SettingsDao
import com.topjohnwu.myfuck.core.myfuckdb.StringDao
import com.topjohnwu.myfuck.core.tasks.RepoUpdater
import com.topjohnwu.myfuck.data.database.RepoDatabase
import com.topjohnwu.myfuck.data.database.SuLogDatabase
import com.topjohnwu.myfuck.data.repository.LogRepository
import com.topjohnwu.myfuck.data.repository.NetworkService
import com.topjohnwu.myfuck.ktx.deviceProtectedContext
import com.topjohnwu.myfuck.ui.home.HomeViewModel
import com.topjohnwu.myfuck.ui.install.InstallViewModel
import com.topjohnwu.myfuck.ui.log.LogViewModel
import com.topjohnwu.myfuck.ui.module.ModuleViewModel
import com.topjohnwu.myfuck.ui.settings.SettingsViewModel
import com.topjohnwu.myfuck.ui.superuser.SuperuserViewModel
import com.topjohnwu.myfuck.ui.surequest.SuRequestViewModel

val AppContext: Context inline get() = ServiceLocator.context

@SuppressLint("StaticFieldLeak")
object ServiceLocator {

    lateinit var context: Context
    val deContext by lazy { context.deviceProtectedContext }
    val timeoutPrefs by lazy { deContext.getSharedPreferences("su_timeout", 0) }

    // Database
    val policyDB = PolicyDao()
    val settingsDB = SettingsDao()
    val stringDB = StringDao()
    val repoDB by lazy { createRepoDatabase(context).repoDao() }
    val sulogDB by lazy { createSuLogDatabase(deContext).suLogDao() }
    val repoUpdater by lazy { RepoUpdater(networkService, repoDB) }
    val logRepo by lazy { LogRepository(sulogDB) }

    // Networking
    val okhttp by lazy { createOkHttpClient(context) }
    val retrofit by lazy { createRetrofit(okhttp) }
    val markwon by lazy { createMarkwon(context, okhttp) }
    val networkService by lazy {
        NetworkService(
            createApiService(retrofit, Const.Url.GITHUB_PAGE_URL),
            createApiService(retrofit, Const.Url.GITHUB_RAW_URL),
            createApiService(retrofit, Const.Url.JS_DELIVR_URL),
            createApiService(retrofit, Const.Url.GITHUB_API_URL)
        )
    }

    object VMFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(clz: Class<T>): T {
            return when (clz) {
                HomeViewModel::class.java -> HomeViewModel(networkService)
                LogViewModel::class.java -> LogViewModel(logRepo)
                ModuleViewModel::class.java -> ModuleViewModel(repoDB, repoUpdater)
                SettingsViewModel::class.java -> SettingsViewModel(repoDB)
                SuperuserViewModel::class.java -> SuperuserViewModel(policyDB)
                InstallViewModel::class.java -> InstallViewModel(networkService)
                SuRequestViewModel::class.java -> SuRequestViewModel(policyDB, timeoutPrefs)
                else -> clz.newInstance()
            } as T
        }
    }
}

inline fun <reified VM : ViewModel> ViewModelStoreOwner.viewModel() =
    lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, ServiceLocator.VMFactory).get(VM::class.java)
    }

private fun createRepoDatabase(context: Context) =
    Room.databaseBuilder(context, RepoDatabase::class.java, "repo.db")
        .fallbackToDestructiveMigration()
        .build()

private fun createSuLogDatabase(context: Context) =
    Room.databaseBuilder(context, SuLogDatabase::class.java, "sulogs.db")
        .fallbackToDestructiveMigration()
        .build()
