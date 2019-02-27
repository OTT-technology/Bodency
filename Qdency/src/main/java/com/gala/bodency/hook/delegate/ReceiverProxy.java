package com.gala.bodency.hook.delegate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dongyu on 2018/12/5.
 */
public class ReceiverProxy extends BroadcastReceiver {
    private static final String TAG = "ReceiverProxy";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "context/intent = " + context + "/" + intent);
    }
}
