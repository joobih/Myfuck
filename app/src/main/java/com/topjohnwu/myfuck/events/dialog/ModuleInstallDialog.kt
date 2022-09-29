package com.topjohnwu.myfuck.events.dialog

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.core.di.ServiceLocator
import com.topjohnwu.myfuck.core.download.Action
import com.topjohnwu.myfuck.core.download.DownloadService
import com.topjohnwu.myfuck.core.download.Subject
import com.topjohnwu.myfuck.core.model.module.OnlineModule
import com.topjohnwu.myfuck.view.MyfuckDialog

class ModuleInstallDialog(private val item: OnlineModule) : MarkDownDialog() {

    private val svc get() = ServiceLocator.networkService

    override suspend fun getMarkdownText(): String {
        val str = svc.fetchString(item.changelog)
        return if (str.length > 1000) str.substring(0, 1000) else str
    }

    override fun build(dialog: MyfuckDialog) {
        super.build(dialog)
        dialog.apply {

            fun download(install: Boolean) {
                val action = if (install) Action.Flash else Action.Download
                val subject = Subject.Module(item, action)
                DownloadService.start(context, subject)
            }

            val title = context.getString(R.string.repo_install_title,
                item.name, item.version, item.versionCode)

            setTitle(title)
            setCancelable(true)
            setButton(MyfuckDialog.ButtonType.NEGATIVE) {
                text = R.string.download
                onClick { download(false) }
            }
            setButton(MyfuckDialog.ButtonType.POSITIVE) {
                text = R.string.install
                onClick { download(true) }
            }
            setButton(MyfuckDialog.ButtonType.NEUTRAL) {
                text = android.R.string.cancel
            }
        }
    }

}
