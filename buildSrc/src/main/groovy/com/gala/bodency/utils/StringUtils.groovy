package com.gala.bodency.utils

class StringUtils {

    static String toString(String[] strs) {
        if (strs == null || strs.length == 0) {
            return ""
        }
        StringBuilder sb = new StringBuilder(strs.length)
        for (s in strs) {
            sb.append(s)
            sb.append(";")
        }
        return sb.toString()
    }

}
