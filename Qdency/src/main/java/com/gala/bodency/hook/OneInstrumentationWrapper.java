package com.gala.bodency.hook;

import android.app.Instrumentation;
import android.content.pm.ProviderInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for redirect activity from Stub(AndroidManifest.xml) to Real(Plugin)
 */
/*package*/ class OneInstrumentationWrapper extends Instrumentation {

    private static final String TAG = "OneInstrumentationWrapper";

    //com.gala.video.hook
    private Instrumentation mBase;

    private List<ProviderInfo> sProviders;
    private List<ProviderInfo> mLazyInitProviders;


    private static final String LAZY = "LAZY";

    public OneInstrumentationWrapper(Instrumentation base) {
        mBase = base;
    }

    public void setProviders(List<ProviderInfo> lp) {
        sProviders = lp;
    }

    @Override
    public boolean onException(Object obj, Throwable e) {
        if (sProviders != null && e.getClass().equals(ClassNotFoundException.class)) {
            boolean errorOnInstallProvider = false;
            StackTraceElement[] stacks = e.getStackTrace();
            for (StackTraceElement st : stacks) {
                if (st.getMethodName().equals("installProvider")) {
                    errorOnInstallProvider = true;
                    break;
                }
            }

            if (errorOnInstallProvider) {
                // We'll reinstall this content provider later, so just ignores it!!!
                // FIXME: any better way to get the class name?
                String msg = e.getMessage();
                final String prefix = "Didn't find class \"";
                if (msg.startsWith(prefix)) {
                    String providerClazz = msg.substring(prefix.length());
                    providerClazz = providerClazz.substring(0, providerClazz.indexOf("\""));
                    for (ProviderInfo info : sProviders) {
                        if (info.name.equals(providerClazz)) {
                            // contentprovider定义在app+stub中时，在handleBindApplication().installContentProviders()
                            // 函数就会执行创建和初始化处理,插件化mix版时，该时机插件还未加载
                            // 会导致加载了host中的class，从而引发class加载 Class ref in pre-verified class 异常
                            // 本方案是在mainfest中将所有provider定义都加了一个后缀"LAZY"，
                            // 这样在installContentProviders()方法中创建对应的provider就会失败
                            // 在失败catch处理中，我们保存provider并去掉"LAZY"后缀，在插件加载完成后再次调用
                            // 可解决该问题
                            String postfix = info.name.substring(info.name.lastIndexOf(".") + 1);
                            if (postfix.equals(LAZY)) {
                                info.name = info.name.substring(0, info.name.lastIndexOf("."));
                            }

                            if (mLazyInitProviders == null) {
                                mLazyInitProviders = new ArrayList<ProviderInfo>();
                            }
                            mLazyInitProviders.add(info);
                            break;
                        }
                    }
                }
                return true;
            }
        }

        return super.onException(obj, e);
    }

}

