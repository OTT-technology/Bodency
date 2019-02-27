package com.gala.bodency.hook.delegate;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;

import com.gala.bodency.hook.DexInstallManager;
import com.gala.bodency.hook.utils.PackageParserCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongyu on 2018/12/17.
 */
public class ProcessService {
    private static File sourceFile = new File(DexInstallManager.getInstance().getContext().getApplicationInfo().sourceDir);
    private static PackageParser.Package mPackage = PackageParserCompat.parsePackage(DexInstallManager.getInstance().getContext(), sourceFile, PackageParser.PARSE_MUST_BE_APK);
    ;
    private static final String TAG = "ProcessService";


    public static ResolveInfo resolveService(Intent intent, int flags) {
        List<ResolveInfo> query = queryIntentServices(intent, flags);
        if (null == query || query.isEmpty()) {
            return null;
        }

        ContentResolver resolver = DexInstallManager.getInstance().getContext().getContentResolver();
        return chooseBestActivity(intent, intent.resolveTypeIfNeeded(resolver), flags, query);
    }

    private static ResolveInfo chooseBestActivity(Intent intent, String s, int flags, List<ResolveInfo> query) {
        return query.get(0);
    }

    private static List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        ComponentName component = intent.getComponent();
        List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
        ContentResolver resolver = DexInstallManager.getInstance().getContext().getContentResolver();

        for (PackageParser.Service service : mPackage.services) {
            if (match(service, component)) {
                ResolveInfo resolveInfo = new ResolveInfo();
                resolveInfo.serviceInfo = service.info;
                resolveInfos.add(resolveInfo);
            } else if (component == null) {
                // only match implicit intent
                for (PackageParser.ServiceIntentInfo intentInfo : service.intents) {
                    if (intentInfo.match(resolver, intent, true, TAG) >= 0) {
                        ResolveInfo resolveInfo = new ResolveInfo();
                        resolveInfo.serviceInfo = service.info;
                        resolveInfos.add(resolveInfo);
                        break;
                    }
                }
            }
        }

        return resolveInfos;
    }

    private static boolean match(PackageParser.Component component, ComponentName target) {
        ComponentName source = component.getComponentName();
        if (source == target) return true;
        if (source != null && target != null
                && source.getClassName().equals(target.getClassName())
                && (source.getPackageName().equals(target.getPackageName())
                || DexInstallManager.getInstance().getContext().getPackageName().equals(target.getPackageName()))) {
            return true;
        }
        return false;
    }


    public static boolean handleService(ResolveInfo resolveInfo) {
        boolean load = true;
        String className = resolveInfo.serviceInfo.name;
        Log.d(TAG, "target className = " + className);
        Log.d(TAG, "launch service = " + className);
        if (className != null && !"".equals(className)) {
            if (resolveInfo.serviceInfo.name.equals("com.gala.bodency.hook.delegate.ServiceProxy")) {
                load = true;
            } else if (!DexInstallManager.getInstance().getInstalled() && !DexInstallManager.getInstance().isAvailableComponent(className)) {
                load = false;
            } else {
                try {
                    ProcessService.class.getClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    load = false;
                    Log.e(TAG, "load class exception load flag = " + load);
                }
            }
        }

        Log.d(TAG, "handle service flag = " + load);
        return load;
    }

    public static ComponentName startDelegateServiceForTarget(Intent target, ServiceInfo serviceInfo, Bundle extras, int command) {
        Log.d(TAG, "start delegate service");
        Intent wrapperIntent = wrapperTargetIntent(target, serviceInfo, extras, command);
        return DexInstallManager.getInstance().getHostContext().startService(wrapperIntent);
    }

    private static Intent wrapperTargetIntent(Intent target, ServiceInfo serviceInfo, Bundle extras, int command) {
        // fill in service with ComponentName
        target.setComponent(new ComponentName(serviceInfo.packageName, serviceInfo.name));
        // start delegate service to run plugin service inside
        Class<? extends Service> delegate = ServiceProxy.class;
        Intent intent = new Intent();
        intent.setClass(DexInstallManager.getInstance().getHostContext(), delegate);
        intent.putExtra(ServiceProxy.EXTRA_TARGET, target);
        intent.putExtra(ServiceProxy.EXTRA_COMMAND, command);
//        intent.putExtra(RemoteService.EXTRA_PLUGIN_LOCATION, pluginLocation);
        if (extras != null) {
            intent.putExtras(extras);
        }
        return intent;
    }
}
