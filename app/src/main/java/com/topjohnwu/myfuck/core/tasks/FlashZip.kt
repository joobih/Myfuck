package com.topjohnwu.myfuck.core.tasks

import android.net.Uri
import androidx.core.net.toFile
import com.topjohnwu.myfuck.core.Const
import com.topjohnwu.myfuck.core.di.AppContext
import com.topjohnwu.myfuck.core.utils.MediaStoreUtils.displayName
import com.topjohnwu.myfuck.core.utils.MediaStoreUtils.inputStream
import com.topjohnwu.myfuck.core.utils.unzip
import com.topjohnwu.myfuck.ktx.writeTo
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

open class FlashZip(
    private val mUri: Uri,
    private val console: MutableList<String>,
    private val logs: MutableList<String>
) {

    private val installDir = File(AppContext.cacheDir, "flash")
    private lateinit var zipFile: File

    @Throws(IOException::class)
    private fun flash(): Boolean {
        installDir.deleteRecursively()
        installDir.mkdirs()

        zipFile = if (mUri.scheme == "file") {
            mUri.toFile()
        } else {
            File(installDir, "install.zip").also {
                console.add("- Copying zip to temp directory")
                try {
                    mUri.inputStream().writeTo(it)
                } catch (e: IOException) {
                    when (e) {
                        is FileNotFoundException -> console.add("! Invalid Uri")
                        else -> console.add("! Cannot copy to cache")
                    }
                    throw e
                }
            }
        }

        val isValid = runCatching {
            zipFile.unzip(installDir, "META-INF/com/google/android", true)
            val script = File(installDir, "updater-script")
            script.readText().contains("#MYFUCK")
        }.getOrElse {
            console.add("! Unzip error")
            throw it
        }

        if (!isValid) {
            console.add("! This zip is not a Myfuck module!")
            return false
        }

        console.add("- Installing ${mUri.displayName}")

        return Shell.cmd("sh $installDir/update-binary dummy 1 \'$zipFile\'")
            .to(console, logs).exec().isSuccess
    }

    open suspend fun exec() = withContext(Dispatchers.IO) {
        try {
            if (!flash()) {
                console.add("! Installation failed")
                false
            } else {
                true
            }
        } catch (e: IOException) {
            Timber.e(e)
            false
        } finally {
            Shell.cmd("cd /", "rm -rf $installDir ${Const.TMPDIR}").submit()
        }
    }
}
