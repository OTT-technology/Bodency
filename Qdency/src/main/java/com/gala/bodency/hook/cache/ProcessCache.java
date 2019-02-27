package com.gala.bodency.hook.cache;

import android.util.Log;

import com.gala.bodency.hook.DexInstallManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongyu on 2018/12/17.
 */
public class ProcessCache {
    private static final String TAG = "ProcessCache";
    private static volatile List<ComponentCache> sCache = new ArrayList<ComponentCache>();

    public static void process(ComponentCache cache) {
        Log.d(TAG, "process component cache = " + cache);
        synchronized (ProcessCache.class) {
            if (DexInstallManager.getInstance().getInstalled()) {
                cache.doTask();
            } else {
                sCache.add(cache);
            }
        }
    }

    /**
     * dex install 完成之后，重发缓存的 component
     */
    public static void postComponentCache() {
        Log.d(TAG, "postComponentCache, component cache size is " + sCache.size());
        synchronized (ProcessCache.class) {
            for (ComponentCache cache : sCache) {
                Log.d(TAG, "postComponentCache, cache is " + cache);
                cache.doTask();
            }
            sCache.clear();
        }
    }
}
