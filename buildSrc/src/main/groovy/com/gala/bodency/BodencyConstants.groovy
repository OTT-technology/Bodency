package com.gala.bodency

import com.gala.bodency.model.BodencyExtension

class BodencyConstants {

    public static String CLASS_PATH_DIR = "/Users/isos/Desktop/release/"

    public static final def TARGET_CLASS = [
            'com.gala.video.app.epg.home.HomeActivityProxy',
            'com.gala.video.app.epg.HomeActivity',
            'com.gala.video.HostApp',
            'com.gala.video.HostAppLogic',
            'com.gala.video.lib.share.uikit2.view.MarqueeTextView',
            'com.gala.video.app.epg.widget.AutoBreakTextView',
            'com.gala.video.lib.share.common.widget.CheckedFocusRelativeLayout',
            'com.gala.video.lib.share.common.widget.checkable.CheckedImageView',
            'com.gala.video.lib.share.common.widget.checkable.CheckedTextView',
            'com.gala.video.lib.share.common.widget.checkable.CheckedView',
            'com.gala.bodency.research.MainActivity']

    public static final def FILTER_CLASS = [
    ]

    /**
     * 'com.gala.video.lib.share.common.activity.QBaseActivity',
     */
    public static final def FILTER_SUPER_CLASS = [
            'com.gala.video.lib.share.common.activity.QMultiScreenActivity',
            'com.gala.video.app.player.BasePlayActivity',
            'com.gala.video.app.epg.ui.applist.activity.AppLauncherActivity',
            'com.gala.video.app.epg.QBaseFragment',
            'android.support.v4.app.FragmentActivity.class'
    ]

    public static final def BODENCY_FILTER_PACKAGE_START = [
            'java',
            'android',
            'dalvik'
    ]

    public static final File CLASS_REPORT_PATH = new File('BodencyReport/ClassReport.txt')
    public static final File METHOD_REPORT_PATH = new File('BodencyReport/MethodReport.txt')

    public static final BodencyExtension BODENCY_EXTENSION = createBodencyExtension()

    private static def createBodencyExtension() {
        BodencyExtension bodencyExtension = new BodencyExtension()
        bodencyExtension.classReportFile = CLASS_REPORT_PATH
        bodencyExtension.methodReportFile = METHOD_REPORT_PATH
        bodencyExtension.targetClass = TARGET_CLASS
        bodencyExtension.filterSuperClass = FILTER_SUPER_CLASS
        bodencyExtension.filterClass = FILTER_CLASS
        return bodencyExtension
    }

}
