package com.gala.bodency.utils

class Logger {

    private static final String[] P_TAG = ['BodencyAnalyser', 'BodencyPlugin', 'BodencyParser', 'MethodAnalyser']

    static void p(TAG, log) {
        if (P_TAG.contains(TAG)) {
            println("$TAG : $log")
        }
    }
}