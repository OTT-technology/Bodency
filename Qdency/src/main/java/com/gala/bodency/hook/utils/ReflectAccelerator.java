package com.gala.bodency.hook.utils;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * User：zhangboo
 * Date：2019/1/10
 * Desc：
 * <p>
 * Copyright (c) 2018 爱奇艺版权所有
 */
public class ReflectAccelerator {

    private static final String TAG = "ReflectAccelerator";

    // ActivityClientRecord
    private static Field sActivityClientRecord_intent_field;
    private static Field sActivityClientRecord_activityInfo_field;
    private static Method sAssetManager_addAssetPath_method;

    public static Intent getIntent(Object/*ActivityClientRecord*/ r) {
        if (sActivityClientRecord_intent_field == null) {
            sActivityClientRecord_intent_field = getDeclaredField(r.getClass(), "intent");
        }
        return getValue(sActivityClientRecord_intent_field, r);
    }

    public static Intent getReceiverIntent(Object/*ActivityThread$ReceiverData*/ data) {
        Field f = getDeclaredField(data.getClass(), "intent");
        return getValue(f, data);
    }

    public static void setActivityInfo(Object/*ActivityClientRecord*/ r, ActivityInfo ai) {
        if (sActivityClientRecord_activityInfo_field == null) {
            sActivityClientRecord_activityInfo_field = getDeclaredField(
                    r.getClass(), "activityInfo");
        }
        setValue(sActivityClientRecord_activityInfo_field, r, ai);
    }

    public static AssetManager newAssetManager() {
        AssetManager assets;
        try {
            assets = AssetManager.class.newInstance();
        } catch (InstantiationException e) {
            Log.e(TAG, "asset manager instantiation exception =", e);
            return null;
        } catch (IllegalAccessException e) {
            Log.e(TAG, "asset manager illegal access exception =", e);
            return null;
        }
        return assets;
    }

    public static int addAssetPath(AssetManager assets, String path) {
        if (sAssetManager_addAssetPath_method == null) {
            sAssetManager_addAssetPath_method = getMethod(AssetManager.class,
                    "addAssetPath", new Class[]{String.class});
        }
        if (sAssetManager_addAssetPath_method == null) return 0;
        Integer ret = invoke(sAssetManager_addAssetPath_method, assets, path);
        if (ret == null) return 0;
        return ret;
    }

    private static Field getDeclaredField(Class cls, String fieldName) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Method getMethod(Class cls, String methodName, Class[] types) {
        try {
            Method method = cls.getMethod(methodName, types);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T> T invoke(Method method, Object target, Object... args) {
        try {
            return (T) method.invoke(target, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void setValue(Field field, Object target, Object value) {
        if (field == null) {
            return;
        }
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static <T> T getValue(Field field, Object target) {
        if (field == null) {
            return null;
        }

        try {
            return (T) field.get(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
