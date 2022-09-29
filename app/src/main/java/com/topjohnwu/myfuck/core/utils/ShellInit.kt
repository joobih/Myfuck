package com.topjohnwu.myfuck.core.utils

import android.content.Context
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.StubApk
import com.topjohnwu.myfuck.core.Config
import com.topjohnwu.myfuck.core.Const
import com.topjohnwu.myfuck.core.Info
import com.topjohnwu.myfuck.core.isRunningAsStub
import com.topjohnwu.myfuck.ktx.cachedFile
import com.topjohnwu.myfuck.ktx.deviceProtectedContext
import com.topjohnwu.myfuck.ktx.rawResource
import com.topjohnwu.myfuck.ktx.writeTo
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import java.io.File
import java.util.jar.JarFile

class ShellInit : Shell.Initializer() {
    override fun onInit(context: Context, shell: Shell): Boolean {
        if (shell.isRoot) {
            Info.isRooted = true
            RootUtils.bindTask?.let { shell.execTask(it) }
            RootUtils.bindTask = null
        }
        shell.newJob().apply {
            add("export ASH_STANDALONE=1")

            val localBB: File
            if (isRunningAsStub) {
                if (!shell.isRoot)
                    return true
                val jar = JarFile(StubApk.current(context))
                val bb = jar.getJarEntry("lib/${Const.CPU_ABI}/libbusybox.so")
                localBB = context.deviceProtectedContext.cachedFile("busybox")
                localBB.delete()
                jar.getInputStream(bb).writeTo(localBB)
                localBB.setExecutable(true)
            } else {
                localBB = File(context.applicationInfo.nativeLibraryDir, "libbusybox.so")
            }

            if (shell.isRoot) {
                add("export MYFUCKTMP=\$(myfuck --path)/.myfuck")
                // Test if we can properly execute stuff in /data
                Info.noDataExec = !shell.newJob().add("$localBB sh -c \"$localBB true\"").exec().isSuccess
            }

            if (Info.noDataExec) {
                // Copy it out of /data to workaround Samsung bullshit
                add(
                    "if [ -x \$MYFUCKTMP/busybox/busybox ]; then",
                    "  cp -af $localBB \$MYFUCKTMP/busybox/busybox",
                    "  exec \$MYFUCKTMP/busybox/busybox sh",
                    "else",
                    "  cp -af $localBB /dev/.busybox",
                    "  exec /dev/.busybox sh",
                    "fi"
                )
            } else {
                // Directly execute the file
                add("exec $localBB sh")
            }

            add(context.rawResource(R.raw.manager))
            if (shell.isRoot) {
                add(context.assets.open("util_functions.sh"))
            }
            add("app_init")
        }.exec()

        fun fastCmd(cmd: String) = ShellUtils.fastCmd(shell, cmd)
        fun getVar(name: String) = fastCmd("echo \$$name")
        fun getBool(name: String) = getVar(name).toBoolean()

        Const.MYFUCKTMP = getVar("MYFUCKTMP")
        Info.isSAR = getBool("SYSTEM_ROOT")
        Info.ramdisk = getBool("RAMDISKEXIST")
        Info.vbmeta = getBool("VBMETAEXIST")
        Info.isAB = getBool("ISAB")
        Info.crypto = getVar("CRYPTOTYPE")

        // Default presets
        Config.recovery = getBool("RECOVERYMODE")
        Config.keepVerity = getBool("KEEPVERITY")
        Config.keepEnc = getBool("KEEPFORCEENCRYPT")
        Config.patchVbmeta = getBool("PATCHVBMETAFLAG")

        return true
    }
}
