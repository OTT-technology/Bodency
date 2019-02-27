package com.gala.bodency.hook;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Keep;
import android.util.Log;

import com.gala.bodency.hook.cache.ActivityCache;
import com.gala.bodency.hook.cache.ReceiverCache;
import com.gala.bodency.hook.utils.ReflectAccelerator;
import com.gala.bodency.qdency.BuildConfig;


/**
 * Created by dongyu on 2017/12/1.
 */
@Keep
class DexInstallCallback implements Handler.Callback {
    private static final String TAG = "DexInstallCallback";

    private final int LAUNCH_ACTIVITY = 100;
    private final int RECEIVER = 113;

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG, "ActivityThread msg.what=" + msg.what);
        switch (msg.what) {
            case LAUNCH_ACTIVITY:
                handleActivityMsg(msg);
                break;
            case RECEIVER:
                handleReceiverMsg(msg);
                break;
            default:
                break;
        }
        return false;
    }

    private void handleActivityMsg(Message msg) {
        Object/*ActivityClientRecord*/ r = msg.obj;
        Log.d(TAG, "r = " + r);
        Intent intent = ReflectAccelerator.getIntent(r);

        ComponentName cn = intent.getComponent();
        String action = intent.getAction();

        Bundle bundle = intent.getExtras();

        Log.d(TAG, "bundle = " + bundle);

        if (cn != null) {
            String className = cn.getClassName();
            Log.d(TAG, "launch activity = " + className + ", " + action);
            if (className != null && !"".equals(className)) {
                if (!DexInstallManager.getInstance().getInstalled() && !DexInstallManager.getInstance().isAvailableComponent(className)) {
                    launchProxyActivity(msg);
                } else {
                    try {
                        Object oj = getClass().getClassLoader().loadClass(className);
                        Log.d(TAG, "oj = " + oj);
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "load class exception = ", e);
                        launchProxyActivity(msg);
                    }
                }
            }
        }
    }

    private void handleReceiverMsg(Message msg) {
        Object/*ReceiverData*/ r = msg.obj;
        Log.d(TAG, "r = " + r);
        Intent intent = ReflectAccelerator.getReceiverIntent(r);
        ComponentName cn = intent.getComponent();

        String action = intent.getAction();

        Log.d(TAG, "launch receiver cn = " + cn);
        if (cn != null) {
            String className = cn.getClassName();
            Log.d(TAG, "launch receiver = " + className + ", " + action);
            if (className != null && !"".equals(className)) {
                if (!DexInstallManager.getInstance().getInstalled() && !DexInstallManager.getInstance().isAvailableComponent(className)) {
                    Log.d(TAG, "dex not installed");
                    launchProxyReceiver(msg);
                } else {
                    try {
                        Object oj = getClass().getClassLoader().loadClass(className);
                        Log.d(TAG, "oj = " + oj);
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "load class exception = ", e);
                        launchProxyReceiver(msg);
                    }
                }
            }
        }
    }

    private void launchProxyActivity(Message msg) {
        Log.d(TAG, "launch proxy activity");
        new ActivityCache<Message>().add(Message.obtain(msg));
        ActivityInfo info = DexInstallManager.getInstance().
                getActivityInfo(DexInstallManager.getInstance().getContext(),
                        "com.gala.bodency.hook.delegate.ActivityProxy");
        if (info == null) {
            Log.d(TAG, "activity info is null");
            return;
        }
        ReflectAccelerator.setActivityInfo(msg.obj, info);
    }

    private void launchProxyReceiver(Message msg) {
        Log.d(TAG, "launch proxy receiver");
        Intent targetIntent = (Intent) ReflectAccelerator.getReceiverIntent(msg.obj).clone();
        new ReceiverCache<Intent>().add(targetIntent);
        ReflectAccelerator.getReceiverIntent(msg.obj).setAction("com.gala.bodency.hook.delegate.ReceiverProxy");
        ReflectAccelerator.getReceiverIntent(msg.obj).setClassName(BuildConfig.APPLICATION_ID, "com.gala.bodency.hook.delegate.ReceiverProxy");
    }

}
