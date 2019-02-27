package com.android.dexdeps;

public class AbstractRef implements HasDeclaringClass {

    protected String mDeclaringClass, mFieldType, mFieldName;

    /**
     * Gets the name of the field's declaring class.
     */
    @Override
    public String getDeclaringClassName() {
        return mDeclaringClass;
    }

    @Override
    public String getClassName() {
        String result = mDeclaringClass.replace('/', '.');
        int startIndex = 1;
        int endIndex = result.length();
        if (result.startsWith("L") || result.startsWith("[L")) {
            startIndex = result.indexOf("L") + 1;
        } else if (result.startsWith("[")) {
            startIndex = result.indexOf("[") + 1;
        }
        if (result.endsWith(";")) {
            endIndex -= 1;
        }
        result = result.substring(startIndex, endIndex);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FieldName -> ");
        sb.append(mFieldName);
        sb.append("; DeclaringClass -> ");
        sb.append(mDeclaringClass);
        sb.append("; TypeName -> ");
        sb.append(mFieldType);
        return sb.toString();
    }
}
