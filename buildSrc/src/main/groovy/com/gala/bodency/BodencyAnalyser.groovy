package com.gala.bodency

import classycle.Analyser
import classycle.Parser
import classycle.UnresolvedNode
import classycle.util.TrueStringPattern
import com.gala.bodency.utils.Logger
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.DirectoryScanner
import org.apache.tools.ant.Project
import org.apache.tools.ant.types.FileSet
import org.apache.tools.ant.types.ResourceCollection
import org.apache.tools.ant.types.ZipFileSet

class BodencyAnalyser {

    private static final String TAG = "BodencyAnalyser"

    private List<ResourceCollection> classResources = new LinkedList<ResourceCollection>()
    private boolean mergeInnerClasses = false
    private Project antProject = null
    private File resultReportFile = null
    private File bodencyReportFile = null
    private File beDependentOnReportFile

    private ArrayList<String> fileNames = new ArrayList<>()
    private ArrayList<String> bodencyTargetFiles = new ArrayList<>()
    private ArrayList<String> bodencyFilterFiles = new ArrayList<>()
    private ArrayList<String> filterFiles = new ArrayList<>()
    private HashSet<String> bodencyResultFiles = new HashSet<>()
    private HashMap<UnresolvedNode, Integer> bodencyResultNode = new HashMap<>()
    private HashMap<String, UnresolvedNode> unresolvedNodes = null

    String[] getClassFileNames() {
        ArrayList<String> fileNames = new ArrayList<String>()
        String fileSeparator = System.getProperty("file.separator")
        for (ResourceCollection rc : classResources) {
            if (!(rc instanceof FileSet)) {
                throw new BuildException("resource collection unsupported " + rc)
            }
            FileSet set = (FileSet) rc
            DirectoryScanner scanner = set.getDirectoryScanner(antProject)
            String path = scanner.getBasedir().getAbsolutePath()
            String[] localFiles = scanner.getIncludedFiles()
            String separator = fileSeparator
            if (rc instanceof ZipFileSet) {
                ZipFileSet zipFileSet = (ZipFileSet) rc
                path = zipFileSet.getSrc().getAbsolutePath()
                separator = Parser.ARCHIVE_PATH_DELIMITER
            }
            for (String localFile : localFiles) {
                fileNames.add(path + separator + localFile)
            }
        }
        this.fileNames.addAll(fileNames)
        String[] classFiles = new String[this.fileNames.size()]
        return this.fileNames.toArray(classFiles)
    }

    void addClassResources(ResourceCollection rc) {
        this.classResources.add(rc)
    }

    void addClassResources(String filePath) {
        this.fileNames.add(filePath)
    }

    void setAntProject(Project antProject) {
        this.antProject = antProject
    }

    boolean isMergeInnerClasses() {
        return mergeInnerClasses
    }

    void setMergeInnerClasses(boolean mergeInnerClasses) {
        this.mergeInnerClasses = mergeInnerClasses
    }

    void setResultReportFile(File resultReportFile) {
        this.resultReportFile = resultReportFile
    }

    void setBodencyReportFile(File bodencyReportFile) {
        this.bodencyReportFile = bodencyReportFile
    }

    ArrayList<String> getBodencyTargetFiles() {
        return bodencyTargetFiles
    }

    void addBodencyTargetFiles(ArrayList<String> bodencyTargetFiles) {
        this.bodencyTargetFiles.addAll(bodencyTargetFiles)
    }

    void setBodencyFilterFiles(ArrayList<String> bodencyFilterFiles) {
        this.bodencyFilterFiles = bodencyFilterFiles
    }

    void setFilterFiles(ArrayList<String> filterFiles) {
        this.filterFiles = filterFiles
    }

    File getBeDependentOnReportFile() {
        return beDependentOnReportFile
    }

    void setBeDependentOnReportFile(File beDependentOnReportFile) {
        this.beDependentOnReportFile = beDependentOnReportFile
    }

    HashSet<String> getBodencyResultFiles() {
        return bodencyResultFiles
    }

    HashMap<String, UnresolvedNode> getUnresolvedNodes() {
        return unresolvedNodes
    }

    long analyze() {
        long time = System.currentTimeMillis()
        Analyser analyser = new Analyser(getClassFileNames(), new TrueStringPattern(), null, isMergeInnerClasses())
        unresolvedNodes = analyser.getUnresolvedNodes()
        findTargetsBodency()
        writeReportFile()
        writeBodencyReportFile()
        writeBeDependentOnReportFile()
        return System.currentTimeMillis() - time
    }

    private void findTargetsBodency() {
        for (targetName in bodencyTargetFiles) {
            findTargetBodency(targetName)
            findTargetBodencyNode(targetName)
        }
    }

    private void findTargetBodency(String name) {
        if (bodencyResultFiles.contains(name) || !unresolvedNodes.containsKey(name)) {
            return
        }
        if (!filterClass(name)) {
            return
        }
        UnresolvedNode node = unresolvedNodes.get(name)
        if (!filterClass(node)) {
            return
        }
        if (name.startsWith("com.gala.video.app.epg") || name.startsWith("com.gala.video.app.player")
                || name.startsWith("com.gala.video.lib.share") || name.startsWith("com.gala.video.lib.framework")) {
            Logger.p(TAG, "name -> ${name}")
        }
        bodencyResultFiles.add(name)
        Iterator<String> iterator = node.linkIterator()
        while (iterator.hasNext()) {
            String className = iterator.next()
            findTargetBodency(className)
        }
    }

    private void findTargetBodencyNode(String name) {
        if (!filterClass(name)) {
            return
        }
        UnresolvedNode node = unresolvedNodes.get(name)
        if (bodencyResultNode.containsKey(node)) {
            int count = bodencyResultNode.get(node)
            bodencyResultNode.put(node, ++count)
            return
        }
        if (!unresolvedNodes.containsKey(name)) {
            return
        }
        if (!filterClass(node)) {
            return
        }
        bodencyResultNode.put(node, 1)
        Iterator<String> iterator = node.linkIterator()
        while (iterator.hasNext()) {
            String className = iterator.next()
            findTargetBodencyNode(className)
        }
    }

    private boolean filterClass(String className) {
        for (name in filterFiles) {
            if (name.equals(className) || className.startsWith(name + '$')) {
                return false
            }
        }
        return true
    }

    private boolean filterClass(UnresolvedNode unresolvedNode) {
        Iterator<String> iterator = unresolvedNode.linkIterator()
        while (iterator.hasNext()) {
            String className = iterator.next()
            if (bodencyFilterFiles.contains(className)) {
                return false
            }
        }
        return true
    }

    private void writeReportFile() {
        PrintWriter printWriter = new PrintWriter(resultReportFile)
        for (name in bodencyResultFiles) {
            name = "${name.replace('.', '/')}.class"
            printWriter.println(name)
        }
        printWriter.flush()
        printWriter.close()
    }

    private void writeBodencyReportFile() {
        PrintWriter printWriter = new PrintWriter(bodencyReportFile)
        List<Map.Entry<UnresolvedNode, Integer>> list = new ArrayList<Map.Entry<UnresolvedNode, Integer>>(bodencyResultNode.entrySet())
        Collections.sort(list, new Comparator<Map.Entry<UnresolvedNode, Integer>>() {
            @Override
            int compare(Map.Entry<UnresolvedNode, Integer> o1, Map.Entry<UnresolvedNode, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue())
            }
        })

        list.each {
            String type = it.getKey().getType()
            String name = it.getKey().getName()
//            if (name.startsWith("com.gala.video.app.epg") || name.startsWith("com.gala.video.app.player")
//                    || name.startsWith("com.gala.video.lib.share") || name.startsWith("com.gala.video.lib.framework")) {
            printWriter.println("type -> ${type}, name -> ${name}, count -> ${it.getValue()}")
            Iterator<String> iterator = it.getKey().linkIterator()
            HashSet<String> temp = new HashSet<>()
            while (iterator.hasNext()) {
                String next = iterator.next()
                if (!temp.contains(next) && !filterPackage(next)) {
                    printWriter.println("\t -- ${next}")
                    temp.add(next)
                }
            }
//            }
        }
        printWriter.flush()
        printWriter.close()
    }

    private boolean filterPackage(String name) {
        for (it in BodencyConstants.BODENCY_FILTER_PACKAGE_START) {
            if (name.startsWith(it)) {
                return true
            }
        }
        return false
    }

    /**
     * 查找被依赖的数据
     * @param dependents 数组获取list
     */
    private HashMap<String, TreeSet<String>> findBeDependentOn() {
        HashMap<String, ArrayList<String>> beDependentOnMap = new HashMap<>()
        unresolvedNodes.each { k, v ->
            Iterator<String> iterator = v.linkIterator()
            while (iterator.hasNext()) {
                String next = iterator.next()
                Set<String> set = beDependentOnMap.get(next)
                if (set == null) {
                    set = new TreeSet<>()
                }
                set.add(k)
                beDependentOnMap.put(next, set)
            }
        }
        return beDependentOnMap
    }

    /**
     * 将被依赖关系写入文件
     */
    private void writeBeDependentOnReportFile() {
        def dependents = ['com.gala.video.lib.share.ifmanager.IInterfaceWrapper']
        def result = findBeDependentOn()

        PrintWriter printWriter = new PrintWriter(beDependentOnReportFile)
        List<Map.Entry<String, TreeSet<String>>> list = new ArrayList<Map.Entry<String, TreeSet<String>>>(result.entrySet())


        list.each {
            String name = it.getKey()
            ArrayList<String> values = it.getValue()
            printWriter.println("name -> ${name}")
            values.each { v ->
                if (!filterPackage(v)) {
                    printWriter.println("\t -- ${v}")
                }
            }
        }
        printWriter.flush()
        printWriter.close()
    }
}
