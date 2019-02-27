package com.gala.bodency.model

class BodencyExtension {

    /**
     * 生成的依赖报告文件
     */
    File classReportFile

    /**
     * 生成的方法报告文件
     */
    File methodReportFile
    /**
     * 需要查找类的依赖
     */
    List<String> targetClass
    /**
     * 过滤直接被依赖类的子类
     */
    List<String> filterSuperClass
    /**
     * 过滤类
     */
    List<String> filterClass

    boolean methodNumber

}
