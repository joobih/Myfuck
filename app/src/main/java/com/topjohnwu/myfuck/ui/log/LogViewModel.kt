package com.topjohnwu.myfuck.ui.log

import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.topjohnwu.myfuck.BR
import com.topjohnwu.myfuck.BuildConfig
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.BaseViewModel
import com.topjohnwu.myfuck.arch.diffListOf
import com.topjohnwu.myfuck.arch.itemBindingOf
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.utils.MediaStoreUtils
import com.topjohnwu.myfuck.core.utils.MediaStoreUtils.outputStream
import com.topjohnwu.myfuck.data.repository.LogRepository
import com.topjohnwu.myfuck.events.SnackbarEvent
import com.topjohnwu.myfuck.ktx.now
import com.topjohnwu.myfuck.ktx.timeFormatStandard
import com.topjohnwu.myfuck.ktx.toTime
import com.topjohnwu.myfuck.utils.set
import com.topjohnwu.myfuck.view.TextItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewModel(
    private val repo: LogRepository
) : BaseViewModel() {

    // --- empty view

    val itemEmpty = TextItem(R.string.log_data_none)
    val itemMyfuckEmpty = TextItem(R.string.log_data_myfuck_none)

    // --- su log

    val items = diffListOf<LogRvItem>()
    val itemBinding = itemBindingOf<LogRvItem> {
        it.bindExtra(BR.viewModel, this)
    }

    // --- myfuck log
    @get:Bindable
    var consoleText = " "
        set(value) = set(value, field, { field = it }, BR.consoleText)

    override fun refresh() = viewModelScope.launch {
        consoleText = repo.fetchMyfuckLogs()
        val (suLogs, diff) = withContext(Dispatchers.Default) {
            val suLogs = repo.fetchSuLogs().map { LogRvItem(it) }
            suLogs to items.calculateDiff(suLogs)
        }
        items.firstOrNull()?.isTop = false
        items.lastOrNull()?.isBottom = false
        items.update(suLogs, diff)
        items.firstOrNull()?.isTop = true
        items.lastOrNull()?.isBottom = true
    }

    fun saveMyfuckLog() = withExternalRW {
        viewModelScope.launch(Dispatchers.IO) {
            val filename = "myfuck_log_%s.log".format(now.toTime(timeFormatStandard))
            val logFile = MediaStoreUtils.getFile(filename, true)
            logFile.uri.outputStream().bufferedWriter().use { file ->
                file.write("---System Properties---\n\n")

                ProcessBuilder("getprop").start()
                    .inputStream.reader().use { it.copyTo(file) }

                file.write("\n---Myfuck Logs---\n")
                file.write("${Info.env.myfuckVersionString} (${Info.env.myfuckVersionCode})\n\n")
                file.write(consoleText)

                file.write("\n---Manager Logs---\n")
                file.write("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n")
                ProcessBuilder("logcat", "-d").start()
                    .inputStream.reader().use { it.copyTo(file) }
            }
            SnackbarEvent(logFile.toString()).publish()
        }
    }

    fun clearMyfuckLog() = repo.clearMyfuckLogs {
        SnackbarEvent(R.string.logs_cleared).publish()
        requestRefresh()
    }

    fun clearLog() = viewModelScope.launch {
        repo.clearLogs()
        SnackbarEvent(R.string.logs_cleared).publish()
        requestRefresh()
    }
}
