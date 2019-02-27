package com.gala.bodency.parser

import com.android.dexdeps.DexData
import com.android.dexdeps.MethodRef
import com.gala.bodency.model.BodencyMethods

class MethodParser {

    private static final String TAG = "MethodParser"

    private def dexs
    private def totalMethod = 0
    BodencyMethods bodencyMethods

    MethodParser(def dexs) {
        this.dexs = dexs
        this.bodencyMethods = new BodencyMethods()
        figureDexsMethod()
    }

    void figureDexsMethod() {
        for (dex in dexs) {
            figureDexMethod(dex)
        }
        bodencyMethods.setTotalMethodCount(totalMethod)
    }

    void figureDexMethod(String dexPath) {
        File file = new File(dexPath)
        figureDexMethod(file)
    }

    void figureDexMethod(File file) {
        if (!file.exists() || !file.isFile()) {
            return
        }
        RandomAccessFile raf = new RandomAccessFile(file, "r")
        DexData dexData = new DexData(raf)
        dexData.load()
        MethodRef[] refs = dexData.getMethodRefs()
        totalMethod += refs.length
        figureMethod(refs)
        raf.close()
    }

    void figureMethod(MethodRef[] refs) {
        for (ref in refs) {
            String className = ref.getClassName()
            String[] splits = className.split("\\.")
            StringBuilder sb = new StringBuilder()
            for (split in splits) {
                sb.append(split)
                bodencyMethods.increaseMethod(sb.toString())
                sb.append(".")
            }
        }
    }

    long getTotalMethod() {
        return totalMethod
    }

    BodencyMethods getBodencyMethods() {
        return bodencyMethods
    }

}
