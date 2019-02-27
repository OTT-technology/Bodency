/*
 * Copyright (C) 2017 Beijing Didi Infinity Technology and Development Co.,Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gala.bodency.hook;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.util.Singleton;

import com.gala.bodency.hook.cache.ProcessCache;
import com.gala.bodency.hook.delegate.ActivityManagerProxy;
import com.gala.bodency.hook.utils.Reflector;
import com.gala.bodency.hook.utils.ThreadUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;

public class DexInstallManager {
    public static final String TAG = "DexInstallManager";
    private static volatile DexInstallManager sInstance = null;
    private static DexInstallCallback sATHandlerCallback = null;
    private static OneInstrumentationWrapper sOneInstrumentation = null;

    // Context of host app
    private Context mContext;
    private Application mApplication;
    private Activity proxyActivity;
    private IActivityManager mActivityManager; // Hooked IActivityManager binder
    private DexInstalledListener mDexInstalledListener;
    private String mFourMainAndroidAppComponents;
    private volatile boolean sInstalled = false;

    public static DexInstallManager getInstance() {
        if (sInstance == null) {
            synchronized (DexInstallManager.class) {
                if (sInstance == null) {
                    sInstance = new DexInstallManager();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        if (context instanceof Application) {
            this.mApplication = (Application) context;
            this.mContext = mApplication.getBaseContext();
        } else {
            final Context app = context.getApplicationContext();
            if (app == null) {
                this.mContext = context;
                this.mApplication = ActivityThread.currentApplication();
            } else {
                this.mApplication = (Application) app;
                this.mContext = mApplication.getBaseContext();
            }
        }
        hookCurrentProcess(context);
    }

    private static void hookMessageHandler(Context context) {
        try {
            Object thread = ActivityThread.currentActivityThread();
            if (thread == null) {
                return;
            }
            Field f = thread.getClass().getDeclaredField("mH");
            f.setAccessible(true);
            Handler ah = (Handler) f.get(thread);
            f = Handler.class.getDeclaredField("mCallback");
            f.setAccessible(true);

            boolean needsInject = false;
            if (sATHandlerCallback == null) {
                needsInject = true;
            } else {
                Object callback = f.get(ah);
                if (callback != sATHandlerCallback) {
                    needsInject = true;
                }
            }

            if (needsInject) {
                // Inject message handler
                sATHandlerCallback = new DexInstallCallback();
                f.set(ah, sATHandlerCallback);
            }
        } catch (Exception e) {
            Log.i(TAG, "Failed to replace message handler");
        }
    }

    private static void hookInstrumentation(Context context) {

        Object/*ActivityThread*/ thread;
        Instrumentation base;

        Field f;

        List<ProviderInfo> providers = null;
        // Get activity thread
        thread = ActivityThread.currentActivityThread();

        // Get providers
        try {
            f = thread.getClass().getDeclaredField("mBoundApplication");
            f.setAccessible(true);
            Object/*AppBindData*/ data = f.get(thread);
            f = data.getClass().getDeclaredField("providers");
            f.setAccessible(true);
            providers = (List<ProviderInfo>) f.get(data);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get providers from thread: " + thread);
        }

        //有contentprovider时，才进行instrumentation的hook
        if (providers != null) {
            // Replace instrumentation
            try {
                f = thread.getClass().getDeclaredField("mInstrumentation");
                f.setAccessible(true);
                base = (Instrumentation) f.get(thread);
                sOneInstrumentation = new OneInstrumentationWrapper(base);
                f.set(thread, sOneInstrumentation);
            } catch (Exception e) {
                throw new RuntimeException("Failed to replace instrumentation for thread: " + thread);
            }

            sOneInstrumentation.setProviders(providers);
        }
    }


    private void hookCurrentProcess(Context context) {
        Log.d(TAG, "hook current process");
        hookSystemServices();
        //com.gala.video.hook instrumentation for contentProvider
        hookInstrumentation(context);
        //com.gala.video.hook mH
        hookMessageHandler(context);
    }

    private ActivityManagerProxy createActivityManagerProxy(IActivityManager origin) throws Exception {
        return new ActivityManagerProxy(this, origin);
    }

    public void install(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "async install");
                // TODO: 2019/2/27 为了显示效果，此处增加延迟
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MultiDex.install(context);
                onDexInstalledSuccess(context, SystemClock.elapsedRealtime() - startTime);
            }
        }).start();
    }

    public boolean getInstalled() {
        return sInstalled;
    }

    /**
     * hookSystemServices, but need to compatible with Android O in future.
     */
    private void hookSystemServices() {
        try {
            Singleton<IActivityManager> defaultSingleton;

            // Build.VERSION_CODES.O = 26, compile SDK 25，不存在此常量
            if (Build.VERSION.SDK_INT >= 26) {
                defaultSingleton = Reflector.on(ActivityManager.class).field("IActivityManagerSingleton").get();
            } else {
                defaultSingleton = Reflector.on(ActivityManagerNative.class).field("gDefault").get();
            }
            IActivityManager origin = defaultSingleton.get();
            IActivityManager activityManagerProxy = (IActivityManager) Proxy.newProxyInstance(mContext.getClassLoader(), new Class[]{IActivityManager.class},
                    createActivityManagerProxy(origin));

            // Hook IActivityManager from ActivityManagerNative
            Reflector.with(defaultSingleton).field("mInstance").set(activityManagerProxy);

            if (defaultSingleton.get() == activityManagerProxy) {
                this.mActivityManager = activityManagerProxy;
                Log.d(TAG, "hookSystemServices succeed : " + mActivityManager);
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    public Context getContext() {
        return mContext;
    }

    public Application getApplication() {
        return mApplication;
    }

    public void setProxyActivity(Activity activity) {
        proxyActivity = activity;
    }

    public Activity getProxyActivity() {
        return proxyActivity;
    }

    public Context getHostContext() {
        return this.mContext;
    }

    public IActivityManager getActivityManager() {
        return this.mActivityManager;
    }

    public ActivityInfo getActivityInfo(Context context, String targetClass) {
        File sourceFile = new File(context.getApplicationInfo().sourceDir);
        BundleParser parser = BundleParser.parsePackage(sourceFile, context.getPackageName(), context);
        parser.collectActivities();
        ActivityInfo[] as = parser.getPackageInfo().activities;

        if (as == null) return null;
        for (ActivityInfo info : as) {
            Log.d(TAG, "activity info = " + info.name);
            if (targetClass.equals(info.name)) {
                return info;
            }
        }

        return null;
    }

    public boolean isAvailableComponent(String name) {
        if (name == null || name.isEmpty()) return false;
        if (mFourMainAndroidAppComponents == null || mFourMainAndroidAppComponents.isEmpty())
            return false;
        return mFourMainAndroidAppComponents.contains(name);
    }

    public void setDexInstalledListener(DexInstalledListener dexInstalledListener) {
        mDexInstalledListener = dexInstalledListener;
    }

    public void onDexInstalledSuccess(Context context, long installCostTime) {
        boolean mainThread = ThreadUtils.isMainThread();
        Log.d(TAG, "onDexInstalledSuccess, isMainThread -> " + mainThread + ", cost time is " + installCostTime + "ms.");
        if (mainThread) {
            sInstalled = true;
            ProcessCache.postComponentCache();
            if (mDexInstalledListener != null) {
                mDexInstalledListener.onSuccess();
            }
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    sInstalled = true;
                    ProcessCache.postComponentCache();
                    if (mDexInstalledListener != null) {
                        mDexInstalledListener.onSuccess();
                    }
                }
            });
        }
    }

    public void setFourMainAndroidAppComponents(String fourMainAndroidAppComponents) {
        mFourMainAndroidAppComponents = fourMainAndroidAppComponents;
    }
}