package com.gala.bodency.hook.delegate;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gala.bodency.hook.DexInstallManager;
import com.gala.bodency.qdency.R;

/**
 * Created by dongyu on 2018/11/26.
 */
public class ActivityProxy extends Activity {
    private static final String TAG = "ActivityProxy";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on create");
        setContentView(R.layout.activity_proxy);
        DexInstallManager.getInstance().setProxyActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "on pause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "on destroy");
    }
}
