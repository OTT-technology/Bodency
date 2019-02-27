package com.gala.bodency.hook;

import android.support.annotation.MainThread;

public interface DexInstalledListener {
    @MainThread
    void onSuccess();
}
