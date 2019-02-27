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

package com.gala.bodency.hook.delegate;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.gala.bodency.hook.DexInstallManager;
import com.gala.bodency.hook.utils.PluginUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author johnsonlee
 */
public class ActivityManagerProxy implements InvocationHandler {
    private static final String TAG = "IActivityManagerProxy";

    private DexInstallManager mPluginManager;
    private IActivityManager mActivityManager;

    public ActivityManagerProxy(DexInstallManager pluginManager, IActivityManager activityManager) {
        this.mPluginManager = pluginManager;
        this.mActivityManager = activityManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d(TAG, "method = " + method.getName());
        if ("startService".equals(method.getName())) {
            try {
                return startService(proxy, method, args);
            } catch (Throwable e) {
                Log.e(TAG, "Start service error", e);
            }
        } else if ("bindService".equals(method.getName())) {
            try {
                return bindService(proxy, method, args);
            } catch (Throwable e) {
                Log.w(TAG, e);
            }
        }

        try {
            // sometimes system binder has problems.
            return method.invoke(this.mActivityManager, args);
        } catch (Throwable th) {
            Throwable c = th.getCause();
            if (c != null && c instanceof DeadObjectException) {
                // retry connect to system binder
                IBinder ams = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                if (ams != null) {
                    IActivityManager am = ActivityManagerNative.asInterface(ams);
                    mActivityManager = am;
                }
            }

            Throwable cause = th;
            do {
                if (cause instanceof RemoteException) {
                    throw cause;
                }
            } while ((cause = cause.getCause()) != null);

            throw c != null ? c : th;
        }

    }

    private Object startService(Object proxy, Method method, Object[] args) throws Throwable {
        IApplicationThread appThread = (IApplicationThread) args[0];
        Intent target = (Intent) args[1];

        ResolveInfo resolveInfo = ProcessService.resolveService(target, 0);
        Log.d(TAG, "start service resolveInfo.serviceInfo = " + resolveInfo);

        if (!ProcessService.handleService(resolveInfo)) {
            return ProcessService.startDelegateServiceForTarget(target, resolveInfo.serviceInfo, null, ServiceProxy.EXTRA_COMMAND_START_SERVICE);
        }

        Log.d(TAG, "start service = " + target);
        return method.invoke(this.mActivityManager, args);
    }

    private Object bindService(Object proxy, Method method, Object[] args) throws Throwable {
        Intent target = (Intent) args[2];

        ResolveInfo resolveInfo = ProcessService.resolveService(target, 0);
        Log.d(TAG, "bind service resolveInfo.serviceInfo = " + resolveInfo);

        if (!ProcessService.handleService(resolveInfo)) {
            Bundle bundle = new Bundle();
            PluginUtil.putBinder(bundle, "sc", (IBinder) args[4]);
            ProcessService.startDelegateServiceForTarget(target, resolveInfo.serviceInfo, bundle, ServiceProxy.EXTRA_COMMAND_BIND_SERVICE);
            return 1;
        }

        Log.d(TAG, "bind service = " + target);
        return method.invoke(this.mActivityManager, args);
    }
}
