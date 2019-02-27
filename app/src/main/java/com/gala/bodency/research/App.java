package com.gala.bodency.research;

import android.app.Application;
import android.content.Context;
import android.os.SystemClock;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.gala.bodency.BuildConfig;
import com.gala.bodency.hook.DexInstallManager;
import com.gala.bodency.hook.DexInstalledListener;
import com.gala.bodency.hook.utils.ThreadUtils;
import com.gala.bodency.research.utils.ConfigUtils;

public class App extends Application implements DexInstalledListener {

    private static final String TAG = "App";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        //    DexInstallManager.getInstance().onDexInstalledSuccess(base, 0);
        //} else
        if (BuildConfig.SUPPORT_ASYNC_MULTI_DEX) {
            boolean isMainProcess = ThreadUtils.isMainThread();
            boolean multiDexInstallSuccess = ConfigUtils.isMultiDexInstallSuccess(base);
            Log.d(TAG, "isMainThread -> " + isMainProcess + ", multiDexInstallSuccess -> " + multiDexInstallSuccess);
            if (isMainProcess && !multiDexInstallSuccess) {
                DexInstallManager.getInstance().init(base);
                DexInstallManager.getInstance().setFourMainAndroidAppComponents(BuildConfig.FOUR_MAIN_ANDROID_APP_COMPONENTS);
                DexInstallManager.getInstance().setDexInstalledListener(this);
                DexInstallManager.getInstance().install(base);
            } else {
                long startTime = SystemClock.elapsedRealtime();
                MultiDex.install(this);
                DexInstallManager.getInstance().onDexInstalledSuccess(base, SystemClock.elapsedRealtime() - startTime);
            }
        } else {
            long startTime = SystemClock.elapsedRealtime();
            Log.d(TAG, "start time = " + startTime);
            MultiDex.install(this);
            long costTime = SystemClock.elapsedRealtime() - startTime;
            DexInstallManager.getInstance().onDexInstalledSuccess(base, costTime);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onSuccess() {
        ConfigUtils.saveMultiDexInstallStatus(this, true);
    }
}
