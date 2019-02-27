package com.gala.bodency.research.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * User：zhangboo
 * Date：2019/2/26
 * Desc：
 * <p>
 * Copyright (c) 2018 爱奇艺版权所有
 */
public class ConfigUtils {

    private static final String PREF_CONFIG_NAME = "config";
    private static final String PREF_KEY_MULTIDEX_INSTALL_STATUS = "is_first_start";

    public static boolean isMultiDexInstallSuccess(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_CONFIG_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(PREF_KEY_MULTIDEX_INSTALL_STATUS, false);
    }

    public static void saveMultiDexInstallStatus(Context context, boolean status) {
        SharedPreferences sp = context.getSharedPreferences(PREF_CONFIG_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(PREF_KEY_MULTIDEX_INSTALL_STATUS, status).apply();
    }
}
