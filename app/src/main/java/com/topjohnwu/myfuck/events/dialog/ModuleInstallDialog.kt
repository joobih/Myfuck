package com.topjohnwu.myfuck.events.dialog

import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.download.Action
import com.topjohnwu.myfuck.core.download.DownloadService
import com.topjohnwu.myfuck.core.download.Subject
import com.topjohnwu.myfuck.core.model.module.OnlineModule
import com.topjohnwu.myfuck.view.MyfuckDialog

class ModuleInstallDialog(private val item: OnlineModule) : DialogEvent() {

    override fun build(dialog: MyfuckDialog) {
        with(dialog) {

            fun download(install: Boolean) {
                val config = if (install) Action.Flash else Action.Download
                val subject = Subject.Module(item, config)
                DownloadService.start(context, subject)
            }

            applyTitle(context.getString(R.string.repo_install_title, item.name))
                .applyMessage(context.getString(R.string.repo_install_msg, item.downloadFilename))
                .cancellable(true)
                .applyButton(MyfuckDialog.ButtonType.NEGATIVE) {
                    titleRes = R.string.download
                    icon = R.drawable.ic_download_md2
                    onClick { download(false) }
                }

            if (Info.env.isActive) {
                applyButton(MyfuckDialog.ButtonType.POSITIVE) {
                    titleRes = R.string.install
                    icon = R.drawable.ic_install
                    onClick { download(true) }
                }
            }

            reveal()
        }
    }

}
