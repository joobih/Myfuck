package com.topjohnwu.myfuck.data.repository

import com.topjohnwu.myfuck.core.Config
import com.topjohnwu.myfuck.core.Config.Value.BETA_CHANNEL
import com.topjohnwu.myfuck.core.Config.Value.CANARY_CHANNEL
import com.topjohnwu.myfuck.core.Config.Value.CUSTOM_CHANNEL
import com.topjohnwu.myfuck.core.Config.Value.DEFAULT_CHANNEL
import com.topjohnwu.myfuck.core.Config.Value.STABLE_CHANNEL
import com.topjohnwu.myfuck.core.Const
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.data.network.*
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class NetworkService(
    private val pages: GithubPageServices,
    private val raw: RawServices,
    private val jsd: JSDelivrServices,
    private val api: GithubApiServices
) {
    suspend fun fetchUpdate() = safe {
        var info = when (Config.updateChannel) {
            DEFAULT_CHANNEL, STABLE_CHANNEL -> fetchStableUpdate()
            BETA_CHANNEL -> fetchBetaUpdate()
            CANARY_CHANNEL -> fetchCanaryUpdate()
            CUSTOM_CHANNEL -> fetchCustomUpdate(Config.customChannelUrl)
            else -> throw IllegalArgumentException()
        }
        if (info.myfuck.versionCode < Info.env.myfuckVersionCode &&
            Config.updateChannel == DEFAULT_CHANNEL
        ) {
            Config.updateChannel = BETA_CHANNEL
            info = fetchBetaUpdate()
        }
        info
    }

    // UpdateInfo
    private suspend fun fetchStableUpdate() = pages.fetchUpdateJSON("stable.json")
    private suspend fun fetchBetaUpdate() = pages.fetchUpdateJSON("beta.json")
    private suspend fun fetchCanaryUpdate() = pages.fetchUpdateJSON("canary.json")
    private suspend fun fetchCustomUpdate(url: String) = raw.fetchCustomUpdate(url)

    private inline fun <T> safe(factory: () -> T): T? {
        return try {
            factory()
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private inline fun <T> wrap(factory: () -> T): T {
        return try {
            factory()
        } catch (e: HttpException) {
            throw IOException(e)
        }
    }

    // Modules related
    suspend fun fetchRepoInfo(url: String = Const.Url.OFFICIAL_REPO) = safe {
        raw.fetchRepoInfo(url)
    }

    // Fetch files
    suspend fun fetchSafetynet() = wrap { jsd.fetchSafetynet() }
    suspend fun fetchBootctl() = wrap { jsd.fetchBootctl() }
    suspend fun fetchInstaller() = wrap {
        val sha = fetchMainVersion()
        jsd.fetchInstaller(sha)
    }
    suspend fun fetchFile(url: String) = wrap { raw.fetchFile(url) }
    suspend fun fetchString(url: String) = wrap { raw.fetchString(url) }

    private suspend fun fetchMainVersion() = api.fetchBranch(MYFUCK_MAIN, "master").commit.sha
}
