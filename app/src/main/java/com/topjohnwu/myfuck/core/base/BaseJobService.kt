package com.topjohnwu.myfuck.core.base

import android.app.job.JobService
import android.content.Context
import com.topjohnwu.myfuck.core.patch

abstract class BaseJobService : JobService() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.patch())
    }
}
