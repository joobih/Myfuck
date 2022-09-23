package com.topjohnwu.myfuck.core.tasks

import com.topjohnwu.myfuck.core.model.module.OnlineModule
import com.topjohnwu.myfuck.data.database.RepoDao
import com.topjohnwu.myfuck.data.repository.NetworkService
import com.topjohnwu.myfuck.ktx.synchronized
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class RepoUpdater(
    private val svc: NetworkService,
    private val repoDB: RepoDao
) {

    suspend fun run(forced: Boolean) = withContext(Dispatchers.IO) {
        val cachedMap = HashMap<String, Date>().also { map ->
            repoDB.getModuleStubs().forEach { map[it.id] = Date(it.last_update) }
        }.synchronized()
        svc.fetchRepoInfo()?.let { info ->
            coroutineScope {
                info.modules.forEach {
                    launch {
                        val lastUpdated = cachedMap.remove(it.id)
                        if (forced || lastUpdated?.before(Date(it.last_update)) != false) {
                            try {
                                val repo = OnlineModule(it).apply { load() }
                                repoDB.addModule(repo)
                            } catch (e: OnlineModule.IllegalRepoException) {
                                Timber.e(e)
                            }
                        }
                    }
                }
            }
            repoDB.removeModules(cachedMap.keys)
        }
    }
}
