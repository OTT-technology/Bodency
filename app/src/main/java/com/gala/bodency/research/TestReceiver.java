package com.gala.bodency.research;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class TestReceiver extends BroadcastReceiver {

    private static final String TAG = "TestReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "TestReceiver 启动成功", Toast.LENGTH_LONG).show();
        Log.i(TAG, "TestReceiver 启动成功");
    }
}
