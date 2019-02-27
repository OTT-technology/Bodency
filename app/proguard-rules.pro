# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
####################################################################################################
#                                                                                                  #
#                              以下为基础混淆规则,请不要轻易修改                                       #
#                                                                                                  #
####################################################################################################
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
# 避免混淆泛型
-keepattributes Signature

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.app.Fragment
-keep public class com.android.vending.licensing.ILicensingService

#包含native方法的类不混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留自定义控件(继承自View)不能被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    *** get* ();

    public *;
    protected *;
}

#枚举类型不混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#序列化的类不混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep class * implements java.io.Serializable {
	*;
}

#R文件中的类不混淆
-keep class **.R$* {
	public *;
}

-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }