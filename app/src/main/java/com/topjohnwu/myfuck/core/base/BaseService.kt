package com.topjohnwu.myfuck.core.base

import android.app.Service
import android.content.Context
import com.topjohnwu.myfuck.core.wrap

abstract class BaseService : Service() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.wrap())
    }
}
