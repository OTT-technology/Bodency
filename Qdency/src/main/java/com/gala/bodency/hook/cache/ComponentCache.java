package com.gala.bodency.hook.cache;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by dongyu on 2018/12/12.
 */
public abstract class ComponentCache<T> {
    private static final String TAG = "ComponentCache";
    private static final Handler mMainHandler = new Handler(Looper.getMainLooper());
    protected LinkedBlockingQueue<T> mQueue = new LinkedBlockingQueue<T>(Integer.MAX_VALUE);

    public void add(T t) {
        if (!isNeedAdd(t)) return;
        Log.d(TAG, "add object = " + t);
        mQueue.add(t);
        ProcessCache.process(this);
    }

    private boolean isNeedAdd(T t) {
        boolean isNeed = true;
        if (mQueue.contains(t)) {
            isNeed = false;
        }
        Log.d(TAG, "is need add flag = " + isNeed);
        return isNeed;
    }

    public void doTask() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                doAction();
            }
        });
    }

    protected void doAction() {

    }
}
