package com.topjohnwu.myfuck;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class DelegateApplication extends Application {

    private Application receiver;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        receiver = DynLoad.createAndSetupApp(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (receiver != null)
            receiver.onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (receiver != null)
            receiver.onConfigurationChanged(newConfig);
    }
}
