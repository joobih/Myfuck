package com.topjohnwu.myfuck.core.repository

import com.topjohnwu.myfuck.core.Const
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.data.SuLogDao
import com.topjohnwu.myfuck.core.model.su.SuLog
import com.topjohnwu.myfuck.ktx.await
import com.topjohnwu.superuser.Shell


class LogRepository(
    private val logDao: SuLogDao
) {

    suspend fun fetchSuLogs() = logDao.fetchAll()

    suspend fun fetchMyfuckLogs(): String {
        val list = object : AbstractMutableList<String>() {
            val buf = StringBuilder()
            override val size get() = 0
            override fun get(index: Int): String = ""
            override fun removeAt(index: Int): String = ""
            override fun set(index: Int, element: String): String = ""
            override fun add(index: Int, element: String) {
                if (element.isNotEmpty()) {
                    buf.append(element)
                    buf.append('\n')
                }
            }
        }
        if (Info.env.isActive) {
            Shell.cmd("cat ${Const.MYFUCK_LOG} || logcat -d -s Myfuck").to(list).await()
        } else {
            Shell.cmd("logcat -d").to(list).await()
        }
        return list.buf.toString()
    }

    suspend fun clearLogs() = logDao.deleteAll()

    fun clearMyfuckLogs(cb: (Shell.Result) -> Unit) =
        Shell.cmd("echo -n > ${Const.MYFUCK_LOG}").submit(cb)

    suspend fun insert(log: SuLog) = logDao.insert(log)

}
