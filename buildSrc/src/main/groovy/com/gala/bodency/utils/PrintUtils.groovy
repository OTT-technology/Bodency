package com.gala.bodency.utils

import classycle.ClassAttributes
import classycle.UnresolvedNode
import classycle.classfile.Constant
import com.gala.bodency.model.BodencyMethods

class PrintUtils {

    static void printUnresolvedNode(String tag, UnresolvedNode unresolvedNode) {
        ClassAttributes attributes = unresolvedNode.getAttributes()
        String type = attributes.getType()
        String name = attributes.getName()
        Logger.p(tag, "printUnresolvedNode, type -> " + type + ", name -> " + name)

        Iterator<String> iterator = unresolvedNode.linkIterator()
        while (iterator.hasNext()) {
            Logger.p(tag, "iterator -> " + iterator.next())
        }
    }

    static void printlnNodeConstantTypeCout(String tag, UnresolvedNode node) {
        String name = node.getAttributes().getName()
        Logger.p(tag, "${name}:" +
                "\n\t - Class count is ${node.mClassCount};" +
                "\n\t - Field Ref count is ${node.mFieldRefCount};" +
                "\n\t - Method Ref count is ${node.mMethodRefCount};" +
                "\n\t - Interface Method Ref count is ${node.mInterfaceMethodRefCount};" +
                "\n\t - String count is ${node.mStringCount};" +
                "\n\t - Int count is ${node.mIntCount};" +
                "\n\t - Float count is ${node.mFloatCount};" +
                "\n\t - Long count is ${node.mLongCount};" +
                "\n\t - Double count is ${node.mDoubleCount};" +
                "\n\t - Name And Type count is ${node.mNameAndTypeCount};" +
                "\n\t - UTF8 count is ${node.mUTF8Count};" +
                "\n\t - Method Handle count is ${node.mMethodHandleCount};" +
                "\n\t - Method Type count is ${node.mMethodTypeCount};" +
                "\n\t - Invoke Dynamic count is ${node.mInvokeDynamicCount};")
    }

    static void printBodencyMethods(String tag, BodencyMethods bodencyMethods) {
        def iterator = bodencyMethods.iterator(' -> ')
        while (iterator.hasNext()) {
            def next = iterator.next()
            Logger.p(tag, next)
        }
    }

    static void printClassConstants(String tag, Constant[] pools) {
        for (pool in pools) {
            Logger.p(tag, pool)
        }
    }

    static void printMethodRefs(def tag, def refs) {
        for (ref in refs) {
            Logger.p(tag, ref)
        }
    }

    static void printFieldRefs(def tag, def fefs) {
        for (fef in fefs) {
            Logger.p(tag, fef)
        }
    }

    static void printMap(tag, def map) {
        for (key in map.keySet()) {
            def value = map.get(key)
            Logger.p(tag, "${key} -> ${value}")
        }
    }

}