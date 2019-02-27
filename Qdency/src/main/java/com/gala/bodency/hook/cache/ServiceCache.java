package com.gala.bodency.hook.cache;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.gala.bodency.hook.DexInstallManager;
import com.gala.bodency.hook.ServiceInfo;
import com.gala.bodency.hook.utils.PluginUtil;
import com.gala.bodency.hook.utils.Reflector;

import java.lang.reflect.Method;

/**
 * Created by dongyu on 2017/12/11.
 */

public class ServiceCache<T> extends ComponentCache<T> {
    private static final String TAG = "ServiceCache";

    public static final int EXTRA_COMMAND_START_SERVICE = 1;
    public static final int EXTRA_COMMAND_STOP_SERVICE = 2;
    public static final int EXTRA_COMMAND_BIND_SERVICE = 3;
    public static final int EXTRA_COMMAND_UNBIND_SERVICE = 4;

    @Override
    protected void doAction() {
        Log.d(TAG, "size = " + mQueue.size());

        while (!mQueue.isEmpty()) {
            ServiceInfo info = null;
            try {
                info = (ServiceInfo) mQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "take message intent/command = " + info.intent + ", " + info.command);

            if (EXTRA_COMMAND_START_SERVICE == info.command) {
                startService(info.intent);
            } else {
                bindService(info.intent);
            }
        }
    }

    private static void startService(Intent intent) {
        Log.d(TAG, "on start command extra intent = " + intent);

        if (null == intent) {
            return;
        }

        ComponentName component = intent.getComponent();
        Log.d(TAG, "on start command extra component = " + component);

        ActivityThread mainThread = ActivityThread.currentActivityThread();
        IApplicationThread appThread = mainThread.getApplicationThread();
        Service service;

        if (DexInstallManager.getInstance().getInstalled()) {
            try {
                service = (Service) ServiceCache.class.getClassLoader().loadClass(component.getClassName()).newInstance();

                Application app = DexInstallManager.getInstance().getApplication();
                IBinder token = appThread.asBinder();
                Method attach = service.getClass().getMethod("attach", Context.class, ActivityThread.class, String.class, IBinder.class, Application.class, Object.class);
                IActivityManager am = DexInstallManager.getInstance().getActivityManager();

                attach.invoke(service, DexInstallManager.getInstance().getContext().getApplicationContext(), mainThread, component.getClassName(), token, app, am);
                service.onCreate();
                service.onStartCommand(intent, 0, 0);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void bindService(Intent intent) {
        Log.d(TAG, "on bind command extra target = " + intent);

        if (null == intent) {
            return;
        }

        ComponentName component = intent.getComponent();
        Log.d(TAG, "on bind command extra component = " + component);

        ActivityThread mainThread = ActivityThread.currentActivityThread();
        IApplicationThread appThread = mainThread.getApplicationThread();
        Service service = null;

        if (DexInstallManager.getInstance().getInstalled()) {
            try {
                service = (Service) ServiceCache.class.getClassLoader().loadClass(component.getClassName()).newInstance();

                Application app = DexInstallManager.getInstance().getApplication();
                IBinder token = appThread.asBinder();
                Method attach = service.getClass().getMethod("attach", Context.class, ActivityThread.class, String.class, IBinder.class, Application.class, Object.class);
                IActivityManager am = DexInstallManager.getInstance().getActivityManager();

                attach.invoke(service, DexInstallManager.getInstance().getContext().getApplicationContext(), mainThread, component.getClassName(), token, app, am);
                service.onCreate();
                service.onStartCommand(intent, 0, 0);
            } catch (Throwable t) {
                Log.e(TAG, "throwable = ", t);
            }

            try {
                IBinder binder = service.onBind(intent);
                IBinder serviceConnection = PluginUtil.getBinder(intent.getExtras(), "sc");
                IServiceConnection iServiceConnection = IServiceConnection.Stub.asInterface(serviceConnection);
                if (Build.VERSION.SDK_INT >= 26) {
                    Log.d(TAG, "on bind command extra appThread 4 = ");

                    iServiceConnection.connected(component, binder, false);
                } else {
                    Log.d(TAG, "on bind command extra appThread 5 = ");
                    Reflector.QuietReflector.with(iServiceConnection).method("connected", ComponentName.class, IBinder.class).call(component, binder);
                }
            } catch (Exception e) {
                Log.e(TAG, "exception = ", e);
            }
        }

    }
}
