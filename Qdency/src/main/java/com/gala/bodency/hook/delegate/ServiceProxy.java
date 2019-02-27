package com.gala.bodency.hook.delegate;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gala.bodency.hook.DexInstallManager;
import com.gala.bodency.hook.ServiceInfo;
import com.gala.bodency.hook.cache.ServiceCache;
import com.gala.bodency.hook.utils.PluginUtil;
import com.gala.bodency.hook.utils.Reflector;

import java.lang.reflect.Method;

/**
 * Created by dongyu on 2018/11/28.
 */
public class ServiceProxy extends Service {
    private static final String TAG = "ServiceProxy";
    /**
     * The target service, usually it's a plugin service intent
     */
    public static final String EXTRA_TARGET = "target";
    public static final String EXTRA_COMMAND = "command";

    public static final int EXTRA_COMMAND_START_SERVICE = 1;
    public static final int EXTRA_COMMAND_STOP_SERVICE = 2;
    public static final int EXTRA_COMMAND_BIND_SERVICE = 3;
    public static final int EXTRA_COMMAND_UNBIND_SERVICE = 4;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "on create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent || !intent.hasExtra(EXTRA_TARGET) || !intent.hasExtra(EXTRA_COMMAND)) {
            return START_STICKY;
        }

        Log.d(TAG, "on start command extra = " + intent.getExtras());

        Intent target = intent.getParcelableExtra(EXTRA_TARGET);

        Log.d(TAG, "on start command extra target = " + target);

        int command = intent.getIntExtra(EXTRA_COMMAND, 0);

        Log.d(TAG, "on start command extra command = " + command);

        if (null == target || command <= 0) {
            return START_STICKY;
        }

        ComponentName component = target.getComponent();

        switch (command) {
            case EXTRA_COMMAND_START_SERVICE: {
                Log.d(TAG, "on start command service component = " + component);
                ActivityThread mainThread = ActivityThread.currentActivityThread();
                IApplicationThread appThread = mainThread.getApplicationThread();
                Service service;

                if (DexInstallManager.getInstance().getInstalled()) {
                    Log.d(TAG, "dex installed");
                    try {
                        service = (Service) getClassLoader().loadClass(component.getClassName()).newInstance();

                        Application app = getApplication();
                        IBinder token = appThread.asBinder();
                        Method attach = service.getClass().getMethod("attach", Context.class, ActivityThread.class, String.class, IBinder.class, Application.class, Object.class);
                        IActivityManager am = DexInstallManager.getInstance().getActivityManager();

                        attach.invoke(service, getApplicationContext(), mainThread, component.getClassName(), token, app, am);
                        service.onCreate();
                        service.onStartCommand(target, 0, 0);
                    } catch (Throwable t) {
                        return START_STICKY;
                    }
                } else {
                    Log.d(TAG, "add service cache");
                    ServiceInfo info = new ServiceInfo();
                    info.intent = target;
                    info.command = EXTRA_COMMAND_START_SERVICE;
                    new ServiceCache<ServiceInfo>().add(info);
                }
                break;
            }
            case EXTRA_COMMAND_BIND_SERVICE: {
                Log.d(TAG, "on bind command service component = " + component);
                ActivityThread mainThread = ActivityThread.currentActivityThread();
                IApplicationThread appThread = mainThread.getApplicationThread();
                Service service = null;

                if (DexInstallManager.getInstance().getInstalled()) {
                    try {
                        Log.d(TAG, "dex installed");
                        service = (Service) getClassLoader().loadClass(component.getClassName()).newInstance();

                        Application app = getApplication();
                        IBinder token = appThread.asBinder();
                        Method attach = service.getClass().getMethod("attach", Context.class, ActivityThread.class, String.class, IBinder.class, Application.class, Object.class);
                        IActivityManager am = DexInstallManager.getInstance().getActivityManager();

                        attach.invoke(service, getApplicationContext(), mainThread, component.getClassName(), token, app, am);
                        service.onCreate();
                    } catch (Throwable t) {
                        Log.w(TAG, t);
                    }
                    try {
                        IBinder binder = service.onBind(target);
                        IBinder serviceConnection = PluginUtil.getBinder(intent.getExtras(), "sc");
                        IServiceConnection iServiceConnection = IServiceConnection.Stub.asInterface(serviceConnection);
                        if (Build.VERSION.SDK_INT >= 26) {
                            iServiceConnection.connected(component, binder, false);
                        } else {
                            Reflector.QuietReflector.with(iServiceConnection).method("connected", ComponentName.class, IBinder.class).call(component, binder);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, e);
                    }
                    break;
                } else {
                    Log.d(TAG, "add service cache");
                    ServiceInfo info = new ServiceInfo();
                    info.intent = intent.getParcelableExtra(EXTRA_TARGET);
                    info.intent.putExtras(intent.getExtras());
                    info.command = EXTRA_COMMAND_BIND_SERVICE;
                    new ServiceCache<ServiceInfo>().add(info);
                }
                break;
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }
}
