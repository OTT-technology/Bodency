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

package com.gala.bodency.hook.utils;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Created by renyugang on 16/8/15.
 */
public class PluginUtil {

    public static final String TAG = "NativeLib";

    public static void putBinder(Bundle bundle, String key, IBinder value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bundle.putBinder(key, value);
        } else {
            Reflector.QuietReflector.with(bundle).method("putIBinder", String.class, IBinder.class).call(key, value);
        }
    }

    public static IBinder getBinder(Bundle bundle, String key) {
        if (bundle == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return bundle.getBinder(key);
        } else {
            return (IBinder) Reflector.QuietReflector.with(bundle)
                    .method("getIBinder", String.class).call(key);
        }
    }

}
