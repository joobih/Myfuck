package com.topjohnwu.myfuck.events.dialog

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.download.DownloadService
import com.topjohnwu.myfuck.core.download.Subject
import com.topjohnwu.myfuck.di.AppContext
import com.topjohnwu.myfuck.di.ServiceLocator
import com.topjohnwu.myfuck.view.MyfuckDialog
import java.io.File

class ManagerInstallDialog : MarkDownDialog() {

    private val svc get() = ServiceLocator.networkService

    override suspend fun getMarkdownText(): String {
        val text = svc.fetchString(Info.remote.myfuck.note)
        // Cache the changelog
        AppContext.cacheDir.listFiles { _, name -> name.endsWith(".md") }.orEmpty().forEach {
            it.delete()
        }
        File(AppContext.cacheDir, "${Info.remote.myfuck.versionCode}.md").writeText(text)
        return text
    }

    override fun build(dialog: MyfuckDialog) {
        super.build(dialog)
        with(dialog) {
            setCancelable(true)
            applyButton(MyfuckDialog.ButtonType.POSITIVE) {
                titleRes = R.string.install
                onClick { DownloadService.start(context, Subject.Manager()) }
            }
            applyButton(MyfuckDialog.ButtonType.NEGATIVE) {
                titleRes = android.R.string.cancel
            }
        }
    }

}
