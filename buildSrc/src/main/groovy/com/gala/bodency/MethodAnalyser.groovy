package com.gala.bodency

import com.gala.bodency.model.BodencyMethods
import com.gala.bodency.parser.MethodParser

class MethodAnalyser {

    private static final String TAG = "MethodAnalyser"
    private def dexsPath
    private def filterClass
    private def filterMethodCount = 0
    private BodencyMethods bodencyMethods
    private File reportFile

    MethodAnalyser() {
    }

    void setDexsPath(dexsPath) {
        this.dexsPath = dexsPath
    }

    void setFilterClass(filterClass) {
        this.filterClass = filterClass
    }

    def getFilterMethodCount() {
        return filterMethodCount
    }

    void figureFilterMethod() {
        for (s in filterClass) {
            filterMethodCount += bodencyMethods.getClassOrPackageMethodCount(s)
        }
    }

    def getDexsMethodCount() {
        return bodencyMethods.getTotalMethodCount()
    }

    void setReportFile(File reportFile) {
        this.reportFile = reportFile
    }

    long analyser() {
        long startTime = System.currentTimeMillis()
        MethodParser methodParser = new MethodParser(dexsPath)
        bodencyMethods = methodParser.getBodencyMethods()
        figureFilterMethod()
        writerReportFile()

        return System.currentTimeMillis() - startTime
    }

    private void writerReportFile() {
        if (reportFile == null) {
            return
        }
        PrintWriter printWriter = new PrintWriter(reportFile)
        Iterator<String> iterator = bodencyMethods.iterator(' -> ')
        while (iterator.hasNext()) {
            def next = iterator.next()
            printWriter.println(next)
        }
        printWriter.flush()
        printWriter.close()
    }
}
