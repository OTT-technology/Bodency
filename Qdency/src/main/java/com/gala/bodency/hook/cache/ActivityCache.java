package com.gala.bodency.hook.cache;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Message;
import android.util.Log;

import com.gala.bodency.hook.DexInstallManager;
import com.gala.bodency.hook.utils.ReflectAccelerator;

/**
 * Created by dongyu on 2017/12/11.
 */

public class ActivityCache<T> extends ComponentCache<T> {
    private static final String TAG = "ActivityCache";

    @Override
    protected void doAction() {
        Log.d(TAG, "size = " + mQueue.size());
        while (!mQueue.isEmpty()) {
            Message msg = null;
            try {
                msg = (Message) mQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "take message msg = " + msg.what);

            Object/*ActivityClientRecord*/ r = msg.obj;
            Log.d(TAG, "r = " + r);

            Intent intent = ReflectAccelerator.getIntent(r);
            ComponentName cn = intent.getComponent();
            String className = cn.getClassName();

            Log.d(TAG, "take message msg className = " + className);
            ActivityInfo info = DexInstallManager.getInstance().
                    getActivityInfo(DexInstallManager.getInstance().getContext(),
                            className);
            if (info == null) {
                Log.d(TAG, "activity info is null");
                return;
            }
            ReflectAccelerator.setActivityInfo(r, info);

            Activity activity = DexInstallManager.getInstance().getProxyActivity();
            Log.d(TAG, "activity = " + activity);
            if (activity != null) {
                Intent it = ReflectAccelerator.getIntent(r);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(it);
            }
        }
    }
}
