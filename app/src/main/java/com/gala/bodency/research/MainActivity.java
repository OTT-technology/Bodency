package com.gala.bodency.research;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gala.bodency.BuildConfig;
import com.gala.bodency.R;
import com.gala.bodency.research.utils.LogUtils;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.d(TAG, "onCreate");
    }

    public void launchActivity(View view) {
        Intent intent = new Intent("com.gala.bodency.research.TestActivity");
        startActivity(intent);
    }

    public void launchService(View view) {
        Intent intent = new Intent("com.gala.bodency.research.TestService");
        intent.setPackage(BuildConfig.APPLICATION_ID);
        startService(intent);
    }

    public void launchReceiver(View view) {
        Intent intent = new Intent("com.gala.bodency.research.TestReceiver");
        sendBroadcast(intent);
    }
}
