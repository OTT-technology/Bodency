package com.gala.bodency.research;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class TestService extends Service {

    private static final String TAG = "TestService";

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "TestService 启动成功", Toast.LENGTH_LONG).show();
        Log.i(TAG, "TestService 启动成功");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }
}
