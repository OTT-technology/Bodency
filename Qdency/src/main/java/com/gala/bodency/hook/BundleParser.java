package com.gala.bodency.hook;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.PatternMatcher;
import android.util.AttributeSet;
import android.util.Log;

import com.gala.bodency.hook.utils.ReflectAccelerator;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User：zhangboo
 * Date：2019/1/10
 * Desc：
 * <p>
 * Copyright (c) 2018 爱奇艺版权所有
 */
public class BundleParser {

    private static final String TAG = "BundleParser";

    /* com.android.internal.R.styleable.* on
     * https://github.com/android/platform_frameworks_base/blob/gingerbread-release/core%2Fres%2Fres%2Fvalues%2Fpublic.xml
     */
    private static final class R {
        public static final class styleable {
            // manifest
            public static final int[] AndroidManifest = {0x0101021b, 0x0101021c};
            public static final int AndroidManifest_versionCode = 0;
            public static final int AndroidManifest_versionName = 1;
            // application
            public static int[] AndroidManifestApplication = {
                    0x01010000, 0x01010001, 0x01010003, 0x010102d3
            };
            public static int AndroidManifestApplication_theme = 0;
            public static int AndroidManifestApplication_label = 1; // for ABIs (Depreciated)
            public static int AndroidManifestApplication_name = 2;
            public static int AndroidManifestApplication_hardwareAccelerated = 3;
            // activity
            public static int[] AndroidManifestActivity = {
                    0x01010000, 0x01010001, 0x01010002, 0x01010003,
                    0x01010011,                                                 //guoxf
                    0x0101001d, 0x0101001e, 0x0101001f, 0x0101022b,
                    0x010102d3
            };
            public static int AndroidManifestActivity_theme = 0;
            public static int AndroidManifestActivity_label = 1;
            public static int AndroidManifestActivity_icon = 2;
            public static int AndroidManifestActivity_name = 3;
            public static int AndroidManifestActivity_process = 4;              //guoxf
            public static int AndroidManifestActivity_launchMode = 5;
            public static int AndroidManifestActivity_screenOrientation = 6;
            public static int AndroidManifestActivity_configChanges = 7;
            public static int AndroidManifestActivity_windowSoftInputMode = 8;
            public static int AndroidManifestActivity_hardwareAccelerated = 9;
            // data (for intent-filter)
            public static int[] AndroidManifestData = {
                    0x01010026, 0x01010027, 0x01010028, 0x01010029,
                    0x0101002a, 0x0101002b, 0x0101002c
            };
            public static int AndroidManifestData_mimeType = 0;
            public static int AndroidManifestData_scheme = 1;
            public static int AndroidManifestData_host = 2;
            public static int AndroidManifestData_port = 3;
            public static int AndroidManifestData_path = 4;
            public static int AndroidManifestData_pathPrefix = 5;
            public static int AndroidManifestData_pathPattern = 6;
        }
    }

    private String mArchiveSourcePath;
    private String mPackageName;
    private PackageInfo mPackageInfo;
    private XmlResourceParser parser;
    private Resources res;
    private ConcurrentHashMap<String, List<IntentFilter>> mActIntentFilters;    //activity intent filters
    private ConcurrentHashMap<String, List<IntentFilter>> mRcvIntentFilters;    //receiver intent filters
    private boolean mUsesHardwareAccelerated;

    private Context mContext;

    public BundleParser(File sourceFile, String packageName, Context context) {
        mArchiveSourcePath = sourceFile.getPath();
        mPackageName = packageName;
        mContext = context;
    }

    public static BundleParser parsePackage(File sourceFile, String packageName, Context context) {
        if (sourceFile == null || !sourceFile.exists() || context == null) {
            return null;
        }

        BundleParser bp = new BundleParser(sourceFile, packageName, context);
        if (!bp.parsePackage()) {
            return null;
        }

        return bp;
    }

    public boolean parsePackage() {
        AssetManager assmgr = null;
        boolean assetError = true;
        try {
            assmgr = ReflectAccelerator.newAssetManager();
            if (assmgr == null) return false;

            int cookie = ReflectAccelerator.addAssetPath(assmgr, mArchiveSourcePath);
            if (cookie != 0) {
                parser = assmgr.openXmlResourceParser(cookie, "AndroidManifest.xml");
                assetError = false;
            } else {
                Log.w(TAG, "Failed adding asset path:" + mArchiveSourcePath);
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to read AndroidManifest.xml of "
                    + mArchiveSourcePath, e);
        }
        if (assetError) {
            if (assmgr != null) assmgr.close();
            return false;
        }

        res = new Resources(assmgr, mContext.getResources().getDisplayMetrics(), null);
        return parsePackage(res, parser);
    }

    private boolean parsePackage(Resources res, XmlResourceParser parser) {
        AttributeSet attrs = parser;
        mPackageInfo = new PackageInfo();

        try {
            int type;
            while ((type = parser.next()) != XmlResourceParser.START_TAG
                    && type != XmlResourceParser.END_DOCUMENT) ;

            // <manifest ...
            mPackageInfo.packageName = parser.getAttributeValue(null, "package").intern();

            // After gradle-small 0.9.0, we roll out
            // `The Small exclusive flags`
            //  F    F    F    F    F    F    F    F
            // 1111 1111 1111 1111 1111 1111 1111 1111
            // ^^^^ ^^^^ ^^^^ ^^^^ ^^^^
            //       ABI Flags (20)
            //                          ^
            //                 nonResources Flag (1)
            //                           ^^^ ^^^^ ^^^^
            //                     platformBuildVersionCode (11) => MAX=0x7FF=4095
            int flags = parser.getAttributeIntValue(null, "platformBuildVersionCode", 0);
            Log.i(TAG, "flags = " + flags);
            int abiFlags = (flags & 0xFFFFF000) >> 12;
            Log.i(TAG, "abiFlags = " + flags);
//            mNonResources = (flags & 0x800) != 0;

            TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifest);
            mPackageInfo.versionCode = sa.getInteger(R.styleable.AndroidManifest_versionCode, 0);
            String versionName = sa.getString(R.styleable.AndroidManifest_versionName);
            if (versionName != null) {
                mPackageInfo.versionName = versionName.intern();
            }

            // <application ...
            while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (type == XmlResourceParser.TEXT) {
                    continue;
                }

                String tagName = parser.getName();
                if (tagName.equals("application")) {
                    ApplicationInfo host = mContext.getApplicationInfo();
                    ApplicationInfo app = new ApplicationInfo(host);

                    sa = res.obtainAttributes(attrs,
                            R.styleable.AndroidManifestApplication);

                    String name = sa.getString(
                            R.styleable.AndroidManifestApplication_name);
                    if (name != null) {
                        app.className = name.intern();
                    } else {
                        app.className = null;
                    }

                    // Get the label value which used as ABI flags.
                    // This is depreciated, we read it from the `platformBuildVersionCode` instead.
                    // TODO: Remove this if the gradle-small 0.9.0 or above being widely used.
//                    if (abiFlags == 0) {
//                        TypedValue label = new TypedValue();
//                        if (sa.getValue(R.styleable.AndroidManifestApplication_label, label)) {
//                            if (label.type == TypedValue.TYPE_STRING) {
//                                abiFlags = Integer.parseInt(label.string.toString());
//                            } else {
//                                abiFlags = label.data;
//                            }
//                        }
//                        if (abiFlags != 0) {
//                            throw new RuntimeException("Please recompile " + mPackageName
//                                    + " use gradle-small 0.9.0 or above");
//                        }
//                    }

                    app.theme = sa.getResourceId(R.styleable.AndroidManifestApplication_theme, 0);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mUsesHardwareAccelerated = sa.getBoolean(
                                R.styleable.AndroidManifestApplication_hardwareAccelerated,
                                host.targetSdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
                    }

                    mPackageInfo.applicationInfo = app;
                    break;
                }
            }

//            if (abiFlags != 0) {
//                IJniHelper jniHelper = SmallInterfaceManager.getJniHelper();
//                String abi = jniHelper.getExtractABI(abiFlags, jniHelper.get64bitFlag());
//                SmallLogUtils.i(TAG, "abi = " + abi);
//                if (abi != null) {
//                    mLibDir = "lib/" + abi + "/";
//                }
//            }

            sa.recycle();
            return true;
        } catch (XmlPullParserException e) {
            Log.w(TAG, "parse package xml pull parser exception = ", e);
        } catch (IOException e) {
            Log.w(TAG, "parse package io exception = ", e);
        }
        return false;
    }

    public boolean collectActivities() {
        if (mPackageInfo == null || mPackageInfo.applicationInfo == null) return false;
        AttributeSet attrs = parser;

        int type;
        try {
            List<ActivityInfo> activities = new ArrayList<ActivityInfo>();
            //receivers cache guoxf
            List<ActivityInfo> receivers = new ArrayList<ActivityInfo>();

            while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (type != XmlResourceParser.START_TAG) {
                    continue;
                }

                String tagName = parser.getName();
                if (tagName.equals("activity")) {
                    //go on
                } else if (tagName.equals("receiver")) {
                    //is receiver?  guoxf
                } else {
                    //not suppert
                    continue;
                }

                // <activity ...
                ActivityInfo ai = new ActivityInfo();
                ai.applicationInfo = mPackageInfo.applicationInfo;
                ai.packageName = ai.applicationInfo.packageName;

                TypedArray sa = res.obtainAttributes(attrs,
                        R.styleable.AndroidManifestActivity);
                String name = sa.getString(R.styleable.AndroidManifestActivity_name);
                if (name != null) {
                    ai.name = ai.targetActivity = buildClassName(mPackageName, name);
                }
                ai.labelRes = sa.getResourceId(R.styleable.AndroidManifestActivity_label, 0);
                ai.icon = sa.getResourceId(R.styleable.AndroidManifestActivity_icon, 0);
                ai.theme = sa.getResourceId(R.styleable.AndroidManifestActivity_theme, 0);
                ai.launchMode = sa.getInteger(R.styleable.AndroidManifestActivity_launchMode, 0);
                //noinspection ResourceType
                ai.screenOrientation = sa.getInt(
                        R.styleable.AndroidManifestActivity_screenOrientation,
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                ai.configChanges = sa.getInt(R.styleable.AndroidManifestActivity_configChanges, 0);
                ai.softInputMode = sa.getInteger(
                        R.styleable.AndroidManifestActivity_windowSoftInputMode, 0);

                //guoxf
                ai.processName = sa.getString(R.styleable.AndroidManifestActivity_process);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    boolean hardwareAccelerated = sa.getBoolean(
                            R.styleable.AndroidManifestActivity_hardwareAccelerated,
                            mUsesHardwareAccelerated);
                    if (hardwareAccelerated) {
                        ai.flags |= ActivityInfo.FLAG_HARDWARE_ACCELERATED;
                    }
                }

                //collect activity
                if (tagName.equals("activity")) {
                    activities.add(ai);
                } else if (tagName.equals("receiver")) {
                    //collect receiver
                    receivers.add(ai);
                }

                sa.recycle();

                // <intent-filter ...
                List<IntentFilter> intents = new ArrayList<IntentFilter>();
                int outerDepth = parser.getDepth();
                while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT
                        && (type != XmlResourceParser.END_TAG
                        || parser.getDepth() > outerDepth)) {
                    if (type == XmlResourceParser.END_TAG || type == XmlResourceParser.TEXT) {
                        continue;
                    }

                    if (parser.getName().equals("intent-filter")) {
                        IntentFilter intent = new IntentFilter();

                        parseIntent(res, parser, attrs, true, true, intent);

                        if (intent.countActions() == 0) {
                            Log.i(TAG, "No actions in intent filter at "
                                    + mArchiveSourcePath + " "
                                    + parser.getPositionDescription());
                        } else {
                            intents.add(intent);
                        }
                    }
                }

                if (intents.size() > 0) {
                    //collect activity
                    if (tagName.equals("activity")) {
                        if (mActIntentFilters == null) {
                            mActIntentFilters = new ConcurrentHashMap<String, List<IntentFilter>>();
                        }
                        mActIntentFilters.put(ai.name, intents);
                    } else if (tagName.equals("receiver")) {
                        //collect receiver
                        if (mRcvIntentFilters == null) {
                            mRcvIntentFilters = new ConcurrentHashMap<String, List<IntentFilter>>();
                        }
                        mRcvIntentFilters.put(ai.name, intents);
                    }
                }
            }

            int N = activities.size();
            if (N > 0) {
                mPackageInfo.activities = new ActivityInfo[N];
                mPackageInfo.activities = activities.toArray(mPackageInfo.activities);
            }

            //add receivers guoxf
            int nRcvrCount = receivers.size();
            if (nRcvrCount > 0) {
                mPackageInfo.receivers = new ActivityInfo[nRcvrCount];
                mPackageInfo.receivers = receivers.toArray(mPackageInfo.receivers);
            }

            return true;
        } catch (XmlPullParserException e) {
            Log.w(TAG, "collect activities xml pull parser exception = ", e);
        } catch (IOException e) {
            Log.w(TAG, "collect activities io exception = ", e);
        }

        return false;
    }

    private static final String ANDROID_RESOURCES
            = "http://schemas.android.com/apk/res/android";

    private boolean parseIntent(Resources res, XmlResourceParser parser, AttributeSet attrs,
                                boolean allowGlobs, boolean allowAutoVerify, IntentFilter outInfo)
            throws XmlPullParserException, IOException {
        TypedArray sa;
        int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT
                && (type != XmlResourceParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlResourceParser.END_TAG || type == XmlResourceParser.TEXT) {
                continue;
            }

            String nodeName = parser.getName();
            if (nodeName.equals("action")) {
                String value = attrs.getAttributeValue(
                        ANDROID_RESOURCES, "name");
                if (value == null || value.length() == 0) {
                    return false;
                }
                skipCurrentTag(parser);

                outInfo.addAction(value);
            } else if (nodeName.equals("category")) {
                String value = attrs.getAttributeValue(
                        ANDROID_RESOURCES, "name");
                if (value == null || value.length() == 0) {
                    return false;
                }
                skipCurrentTag(parser);

                outInfo.addCategory(value);

            } else if (nodeName.equals("data")) {
                sa = res.obtainAttributes(attrs,
                        R.styleable.AndroidManifestData);

                String str = sa.getString(
                        R.styleable.AndroidManifestData_mimeType);
                if (str != null) {
                    try {
                        outInfo.addDataType(str);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        sa.recycle();
                        return false;
                    }
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_scheme);
                if (str != null) {
                    outInfo.addDataScheme(str);
                }

                String host = sa.getString(
                        R.styleable.AndroidManifestData_host);
                String port = sa.getString(
                        R.styleable.AndroidManifestData_port);
                if (host != null) {
                    outInfo.addDataAuthority(host, port);
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_path);
                if (str != null) {
                    outInfo.addDataPath(str, PatternMatcher.PATTERN_LITERAL);
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_pathPrefix);
                if (str != null) {
                    outInfo.addDataPath(str, PatternMatcher.PATTERN_PREFIX);
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_pathPattern);
                if (str != null) {
                    if (!allowGlobs) {
                        return false;
                    }
                    outInfo.addDataPath(str, PatternMatcher.PATTERN_SIMPLE_GLOB);
                }

                sa.recycle();
                skipCurrentTag(parser);
            } else {
                return false;
            }
        }

        return true;
    }

    private static void skipCurrentTag(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT
                && (type != XmlResourceParser.END_TAG
                || parser.getDepth() > outerDepth)) {
        }
    }

    private static String buildClassName(String pkg, CharSequence clsSeq) {
        if (clsSeq == null || clsSeq.length() <= 0) {
            return null;
        }
        String cls = clsSeq.toString();
        char c = cls.charAt(0);
        if (c == '.') {
            return (pkg + cls).intern();
        }
        if (cls.indexOf('.') < 0) {
            StringBuilder b = new StringBuilder(pkg);
            b.append('.');
            b.append(cls);
            return b.toString().intern();
        }
        if (c >= 'a' && c <= 'z') {
            return cls.intern();
        }
        return null;
    }

    public PackageInfo getPackageInfo() {
        return mPackageInfo;
    }
}
