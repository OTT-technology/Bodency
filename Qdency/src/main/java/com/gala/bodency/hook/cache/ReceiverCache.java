package com.gala.bodency.hook.cache;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.gala.bodency.hook.DexInstallManager;

/**
 * Created by dongyu on 2018/12/5.
 */
public class ReceiverCache<T> extends ComponentCache<T> {
    private static final String TAG = "ReceiverCache";
    private static final String SYSTEM_RECEIVER_PREFIX = "android.";
    private static final String PROXY_RECEIVER_SUFFIX = ".PROXY";

    @Override
    protected void doAction() {
        Log.d(TAG, "size = " + mQueue.size());

        while (!mQueue.isEmpty()) {
            Intent info = null;
            try {
                info = (Intent) mQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "intent = " + info);
            if (info != null) {

                String action = info.getAction();
                /*
                 * 由于系统广播，应用无法进行发送，所以系统广播添加后缀后再进行重发，接收方在注册时候，同时需要注册同名且有代理后缀的 action
                 * 例如：
                 * <intent-filter>
                 *     <action android:name="android.intent.action.BOOT_COMPLETED" />
                 *     <action android:name="android.intent.action.BOOT_COMPLETED.PROXY" />
                 * </intent-filter>
                 */
                if (needProxy(action)) {
                    action += PROXY_RECEIVER_SUFFIX;
                    info.setAction(action);
                }
                DexInstallManager.getInstance().getContext().sendBroadcast(info);
            }
        }
    }

    /**
     * 判断广播重发是否需要添加代理后缀
     * 由于系统广播，应用无法进行发送，所以系统广播添加后缀后再进行重发，接收方在注册时候，同时需要注册同名且有代理后缀的 action
     * 例如：
     * <intent-filter>
     * <action android:name="android.intent.action.BOOT_COMPLETED" />
     * <action android:name="android.intent.action.BOOT_COMPLETED.PROXY" />
     * </intent-filter>
     *
     * @param action
     * @return
     */
    private boolean needProxy(String action) {
        return !TextUtils.isEmpty(action) && action.startsWith(SYSTEM_RECEIVER_PREFIX) && !action.endsWith(PROXY_RECEIVER_SUFFIX);
    }

}
