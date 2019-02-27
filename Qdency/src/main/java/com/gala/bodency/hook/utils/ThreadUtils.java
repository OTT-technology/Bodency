package com.gala.bodency.hook.utils;

import android.os.Looper;

/**
 * User：zhangboo
 * Date：2019/2/26
 * Desc：
 * <p>
 * Copyright (c) 2018 爱奇艺版权所有
 */
public class ThreadUtils {

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }
}
