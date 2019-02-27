package com.gala.bodency

import com.gala.bodency.model.BodencyExtension
import com.gala.bodency.utils.Logger
import groovy.io.FileType
import org.apache.tools.ant.types.FileSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.util.GFileUtils

class BodencyPlugin implements Plugin<Project> {

    private static final String TAG = "BodencyPlugin"
    private static final String PLUGIN_NAME = "bodency"
    protected Project project
    private BodencyExtension bodencyExtension
    private def bodencyClass

    @Override
    void apply(Project project) {
        this.project = project
        Logger.p(TAG, "apply -> ${project.name}")
        bodencyExtension = project.extensions.create(PLUGIN_NAME, BodencyExtension)

        android.applicationVariants.all { variant ->
            def variantName = variant.name.capitalize()
            def buildTypeName = variant.buildType.name
            def minifyEnabled = variant.buildType.minifyEnabled
            Logger.p(TAG, "${buildTypeName}, minifyEnabled -> ${minifyEnabled}")

            cleanFile(buildTypeName)
            Task jarTask
            if (minifyEnabled) {
                jarTask = project.tasks.findByName("transformClassesAndResourcesWithProguardFor${variantName}")
            } else {
                jarTask = project.tasks.findByName("transformClassesWithJarMergingFor${variantName}")
            }

            if (jarTask == null) {
                jarTask = project.tasks.findByName("transformResourcesWithMergeJavaResFor${variantName}")
            }

            if (jarTask == null) {
                Logger.p(TAG, "jarTask is null.")
                return
            }

            File classDir
            jarTask.doLast {
                Logger.p(TAG, "jarTask.doLast ${jarTask.name}")
                File file = jarTask.streamOutputFolder
                file.eachFileRecurse(FileType.FILES, {
                    if (it.name.endsWith(".jar")) {
                        classDir = it.getParentFile()
                    }
                })
                File classDestination = createFile("$buildTypeName${File.separator}class")
                GFileUtils.copyDirectory(classDir, classDestination)
                classDir = classDestination
                Logger.p(TAG, "${buildTypeName}, jarTask.classDir -> ${classDir}")
            }

            Task multidexListTask = project.tasks.findByName("transformClassesWithMultidexlistFor${variantName}")
            multidexListTask.doFirst {
                classAnalyserExecute(buildTypeName, project, classDir)
            }

            if (bodencyExtension.methodNumber) {
                Task dexTask = project.tasks.findByName("transformClassesWithDexFor${variantName}")
                if (dexTask == null) {
                    dexTask = project.tasks.findByName("transformDexArchiveWithDexMergerFor${variantName}")
                }
                dexTask.doLast {
                    File file = dexTask.streamOutputFolder
                    def dexsPath = []
                    File dexDestination = createFile("$buildTypeName${File.separator}dex")
                    file.eachFileRecurse(FileType.FILES, {
                        Logger.p(TAG, "transformClassesWithDex.it -> ${it.getAbsolutePath()}")
                        if (it.name.endsWith('.dex')) {
                            GFileUtils.copyFile(it, new File("${dexDestination.getAbsolutePath()}${File.separator}${it.name}"))
                            dexsPath.add("${dexDestination.getAbsolutePath()}${File.separator}${it.name}")
                            Logger.p(TAG, "it.name -> ${dexDestination.getAbsolutePath()}${File.separator}${it.name}")
                        }
                    })
                    methodAnalyserExecute(buildTypeName, dexsPath)
                }
            }
        }

        project.tasks.findByName("clean").doLast {
            cleanFile(null)
        }
    }

    protected com.android.build.gradle.BaseExtension getAndroid() {
        return project.android
    }

    protected File getReportFile(buildType) {
        return createFile("$buildType${File.separator}report.txt")
    }

    protected File createFile(String name) {
        def file = new File(project.getBuildDir().absolutePath + File.separator + PLUGIN_NAME + File.separator + name)
        file.getParentFile().mkdirs()
        return file
    }

    void classAnalyserExecute(def buildTypeName, Project project, File dir) {
        BodencyAnalyser analyser = new BodencyAnalyser()
        FileSet fileSet = new FileSet()
        if (dir == null || !dir.exists()) {
            return
        }
        fileSet.setDir(dir)
        analyser.addClassResources(fileSet)
        analyser.setAntProject(project.getAnt().getProject())
        analyser.addBodencyTargetFiles(bodencyExtension.targetClass)
        analyser.setBodencyFilterFiles(bodencyExtension.filterSuperClass)
        analyser.setFilterFiles(bodencyExtension.filterClass)
        analyser.setResultReportFile(bodencyExtension.classReportFile)
        analyser.setBodencyReportFile(createFile("$buildTypeName${File.separator}bodency_report.txt"))
        analyser.setBeDependentOnReportFile(createFile("$buildTypeName${File.separator}be_dependent_report.txt"))
        def time = analyser.analyze()

        bodencyClass = analyser.getBodencyResultFiles()

        def totalCount = analyser.getUnresolvedNodes().size()
        def resultCount = analyser.getBodencyResultFiles().size()

        def text = "Analyze file total count is ${totalCount}.\nThe result file has ${resultCount}.\nBodencyPlugin analysis time was ${time}ms."
        writerReportFile(buildTypeName, text)
        Logger.p(TAG, text)
    }

    void methodAnalyserExecute(def buildTypeName, def dexsPath) {
        MethodAnalyser analyser = new MethodAnalyser()
        analyser.setDexsPath(dexsPath)
        analyser.setFilterClass(bodencyClass)
        analyser.setReportFile(createFile("$buildTypeName${File.separator}method_report.txt"))
        def time = analyser.analyser()

        def totalCount = analyser.getDexsMethodCount()
        def bodencyCount = analyser.getFilterMethodCount()

        def text = "\nDex methods count is ${totalCount}.\nThe total number of dex methods is ${bodencyCount}.\nMethod analysis time was ${time}ms."
        writerReportFile(buildTypeName, text)
        Logger.p(TAG, text)
    }

    void writerReportFile(buildTypeName, text) {
        File reportFile = getReportFile(buildTypeName)
        reportFile.append(text)
    }

    void cleanFile(folder) {
        def path = project.getProjectDir().absolutePath + File.separator + PLUGIN_NAME
        if (folder != null) {
            path = "${path}${File.separator}${folder}"
        }
        File bodency = new File(path)
        if (bodency.exists()) {
            bodency.deleteDir()
        }
    }
}
