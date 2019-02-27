/*
 * Copyright (c) 2003-2008, Franz-Josef Elmer, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package classycle;

import com.gala.bodency.utils.Logger;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import classycle.classfile.ClassConstant;
import classycle.classfile.Constant;
import classycle.classfile.StringConstant;
import classycle.classfile.UTF8Constant;
import classycle.graph.AtomicVertex;
import classycle.util.StringPattern;
import classycle.util.TrueStringPattern;

/**
 * Utility methods for parsing class files and creating directed graphs.
 * The nodes of the graph are classes. The initial vertex of an edge is the
 * class which uses the class specified by the terminal vertex.
 *
 * @author Franz-Josef Elmer
 */
public class Parser {

    public static final String TAG = "Parser";

    public static final String ARCHIVE_PATH_DELIMITER = "::";
    private static final int ACC_INTERFACE = 0x200, ACC_ABSTRACT = 0x400;
    private static final String[] ZIP_FILE_TYPES = new String[]{".zip", ".jar", ".war", ".ear"};
    private static final FileFilter ZIP_FILE_FILTER = new FileFilter() {
        public boolean accept(File file) {
            return isZipFile(file);
        }
    };

    /**
     * Private constructor to prohibit instantiation.
     */
    private Parser() {
    }

    /**
     * Reads and parses class files and creates a direct graph. Short-cut of
     * <tt>readClassFiles(classFiles, new {@link TrueStringPattern}(),
     * null, false);</tt>
     */
    public static AtomicVertex[] readClassFiles(String[] classFiles) throws IOException {
        return readClassFiles(classFiles, new TrueStringPattern(), null, false);
    }

    /**
     * Reads the specified class files and creates a directed graph where each
     * vertex represents a class. The head vertex of an arc is a class which is
     * used by the tail vertex of the arc.
     * The elements of <tt>classFiles</tt> are file names (relative to the
     * working directory) which are interpreted depending on its file type as
     * <ul>
     * <li>path of a class file (file type <tt>.class</tt>)
     * <li>path of a class file inside a ZIP file. The path has to contain both paths:
     * the path of the ZIP file first and the path of the class file in the ZIP file second.
     * Both have to be separated by '::'.
     * <li>path of a file of type <code>.zip</code>, <code>.jar</code>,
     * <code>.war</code>, or <code>.ear</code>
     * containing class file
     * <li>path of a folder containing class files or zip/jar/war/ear files
     * </ul>
     * Folders and zip/jar/war/ear files are searched recursively
     * for class files. If a folder is specified only the top-level
     * zip/jar/war/ear files of that folder are analysed.
     *
     * @param classFiles        Array of file paths.
     * @param pattern           Pattern fully qualified class names have to match in order
     *                          to be added to the graph. Otherwise they count as
     *                          'external'.
     * @param reflectionPattern Pattern ordinary string constants of a class
     *                          file have to fullfill in order to be handled as
     *                          class references. In addition they have to be
     *                          syntactically valid fully qualified class names. If
     *                          <tt>null</tt> ordinary string constants will not be
     *                          checked.
     * @param mergeInnerClasses If <code>true</code>
     *                          merge inner classes with its outer class
     * @return directed graph.
     */
    public static AtomicVertex[] readClassFiles(String[] classFiles, StringPattern pattern,
                                                StringPattern reflectionPattern, boolean mergeInnerClasses)
            throws IOException {
        Logger.p(TAG, "reflectionPattern -> " + reflectionPattern + ", pattern -> " + pattern.getClass());
        ArrayList<UnresolvedNode> unresolvedNodes = new ArrayList<UnresolvedNode>();
        Map<String, ZipFile> archives = new HashMap<String, ZipFile>();
        for (int i = 0; i < classFiles.length; i++) {
            String classFile = classFiles[i];
            int indexOfDelimiter = classFile.indexOf(ARCHIVE_PATH_DELIMITER);
            if (indexOfDelimiter >= 0) {
                String archivePath = classFile.substring(0, indexOfDelimiter);
                ZipFile zipFile = archives.get(archivePath);
                if (zipFile == null) {
                    zipFile = new ZipFile(archivePath);
                    archives.put(archivePath, zipFile);
                }
                classFile = classFile.substring(indexOfDelimiter + ARCHIVE_PATH_DELIMITER.length());
                ZipEntry entry = zipFile.getEntry(classFile);
                analyseClassFileFromZipEntry(zipFile, entry, archivePath, unresolvedNodes, reflectionPattern);
                continue;
            }
            File file = new File(classFile);
            if (file.isDirectory()) {
                analyseClassFile(file, classFile, unresolvedNodes, reflectionPattern);
                File[] files = file.listFiles(ZIP_FILE_FILTER);
                for (int j = 0; j < files.length; j++) {
                    String source = createSourceName(classFile, files[j].getName());
                    analyseClassFiles(new ZipFile(files[j].getAbsoluteFile()), source,
                            unresolvedNodes, reflectionPattern);
                }
            } else if (file.getName().endsWith(".class")) {
                analyseClassFile(file, null, unresolvedNodes, reflectionPattern);
            } else if (isZipFile(file)) {
                analyseClassFiles(new ZipFile(file.getAbsoluteFile()), classFile,
                        unresolvedNodes, reflectionPattern);
            } else {
                throw new IOException(classFile + " is an invalid file.");
            }
        }
        List<UnresolvedNode> filteredNodes = new ArrayList<UnresolvedNode>();
        for (UnresolvedNode node : unresolvedNodes) {
            if (node.isMatchedBy(pattern)) {
                filteredNodes.add(node);
            }
        }
        UnresolvedNode[] nodes = new UnresolvedNode[filteredNodes.size()];
        nodes = (UnresolvedNode[]) filteredNodes.toArray(nodes);
        return GraphBuilder.createGraph(nodes, mergeInnerClasses);
    }

    private static String createSourceName(String classFile, String name) {
        return classFile + (classFile.endsWith(File.separator) ? name
                : File.separatorChar + name);
    }

    private static boolean isZipFile(File file) {
        boolean result = false;
        String name = file.getName().toLowerCase();
        for (int i = 0; i < ZIP_FILE_TYPES.length; i++) {
            if (name.endsWith(ZIP_FILE_TYPES[i])) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static void analyseClassFile(File file, String source, ArrayList<UnresolvedNode> unresolvedNodes,
                                         StringPattern reflectionPattern) throws IOException {
        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                File child = new File(file, files[i]);
                if (child.isDirectory() || files[i].endsWith(".class")) {
                    analyseClassFile(child, source, unresolvedNodes, reflectionPattern);
                }
            }
        } else {
            unresolvedNodes.add(extractNode(file, source, reflectionPattern));
        }
    }

    private static UnresolvedNode extractNode(File file, String source, StringPattern reflectionPattern)
            throws IOException {
        InputStream stream = null;
        UnresolvedNode result = null;
        try {
            stream = new FileInputStream(file);
            result = Parser.createNode(stream, source, (int) file.length(),
                    reflectionPattern);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    private static void analyseClassFiles(ZipFile zipFile, String source,
                                          ArrayList<UnresolvedNode> unresolvedNodes, StringPattern reflectionPattern)
            throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            Logger.p(TAG, "analyseClassFiles, zipFile -> " + zipFile.getName() + ", entry -> " + entry);
            analyseClassFileFromZipEntry(zipFile, entry, source, unresolvedNodes, reflectionPattern);
        }
    }

    private static void analyseClassFileFromZipEntry(ZipFile zipFile, ZipEntry entry, String source,
                                                     ArrayList<UnresolvedNode> unresolvedNodes, StringPattern reflectionPattern)
            throws IOException {
        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
            InputStream stream = zipFile.getInputStream(entry);
            int size = (int) entry.getSize();
            unresolvedNodes.add(Parser.createNode(stream, source, size, reflectionPattern));
        }
    }

    /**
     * Creates a new node with unresolved references.
     *
     * @param stream            A just opended byte stream of a class file.
     *                          If this method finishes succefully the internal pointer of the
     *                          stream will point onto the superclass index.
     * @param source            Optional source of the class file. Can be <code>null</code>.
     * @param size              Number of bytes of the class file.
     * @param reflectionPattern Pattern used to check whether a
     *                          {@link StringConstant} refer to a class. Can be <tt>null</tt>.
     * @return a node with unresolved link of all classes used by the analysed
     * class.
     */
    public static UnresolvedNode createNode(InputStream stream, String source, int size,
                                            StringPattern reflectionPattern)
            throws IOException {
        Logger.p(TAG, "source -> " + source + ", reflectionPattern -> " + reflectionPattern);
        // Reads constant pool, accessFlags, and class name
        DataInputStream dataStream = new DataInputStream(stream);
        Constant[] pool = Constant.extractConstantPool(dataStream);
        int accessFlags = dataStream.readUnsignedShort();
        String name = ((ClassConstant) pool[dataStream.readUnsignedShort()]).getName();
        ClassAttributes attributes = null;
        if ((accessFlags & ACC_INTERFACE) != 0) {
            attributes = ClassAttributes.createInterface(name, source, size);
        } else {
            if ((accessFlags & ACC_ABSTRACT) != 0) {
                attributes = ClassAttributes.createAbstractClass(name, source, size);
            } else {
                attributes = ClassAttributes.createClass(name, source, size);
            }
        }

        // Creates a new node with unresolved references
        UnresolvedNode node = new UnresolvedNode();
        node.setAttributes(attributes);
        for (int i = 0; i < pool.length; i++) {
            Constant constant = pool[i];
            if (constant instanceof ClassConstant) {
                ClassConstant cc = (ClassConstant) constant;
                if (!cc.getName().startsWith(("[")) && !cc.getName().equals(name)) {
                    node.addLinkTo(cc.getName());
                }
            } else if (constant instanceof UTF8Constant) {
                parseUTF8Constant((UTF8Constant) constant, node, name);
            } else if (reflectionPattern != null && constant instanceof StringConstant) {
                String str = ((StringConstant) constant).getString();
                if (ClassNameExtractor.isValid(str) && reflectionPattern.matches(str)) {
                    node.addLinkTo(str);
                }
            }
        }
        System.out.println("\n\n");
        Logger.p(TAG, "--------------------------------------------");
        Logger.p(TAG, "node -> " + node.getAttributes().toString());
        Iterator<String> stringIterator = node.linkIterator();
        while (stringIterator.hasNext()) {
            Logger.p(TAG, "stringIterator -> " + stringIterator.next());
        }
        return node;
    }

    /**
     * Parses an UFT8Constant and picks class names if it has the correct syntax
     * of a field or method descirptor.
     */
    static void parseUTF8Constant(UTF8Constant constant, UnresolvedNode node, String className) {
        Set<String> classNames = new ClassNameExtractor(constant).extract();
        for (String element : classNames) {
            if (!className.equals(element)) {
                node.addLinkTo(element);
            }
        }
    }

    public static UnresolvedNode[] getUnresolvedNodes(String[] classFiles, StringPattern pattern,
                                                      StringPattern reflectionPattern, boolean mergeInnerClasses)
            throws IOException {
        Logger.p(TAG, "reflectionPattern -> " + reflectionPattern + ", pattern -> " + pattern.getClass());
        ArrayList<UnresolvedNode> unresolvedNodes = new ArrayList<UnresolvedNode>();
        Map<String, ZipFile> archives = new HashMap<String, ZipFile>();
        for (int i = 0; i < classFiles.length; i++) {
            String classFile = classFiles[i];
            int indexOfDelimiter = classFile.indexOf(ARCHIVE_PATH_DELIMITER);
            if (indexOfDelimiter >= 0) {
                String archivePath = classFile.substring(0, indexOfDelimiter);
                ZipFile zipFile = archives.get(archivePath);
                if (zipFile == null) {
                    zipFile = new ZipFile(archivePath);
                    archives.put(archivePath, zipFile);
                }
                classFile = classFile.substring(indexOfDelimiter + ARCHIVE_PATH_DELIMITER.length());
                ZipEntry entry = zipFile.getEntry(classFile);
                analyseClassFileFromZipEntry(zipFile, entry, archivePath, unresolvedNodes, reflectionPattern);
                continue;
            }
            File file = new File(classFile);
            if (file.isDirectory()) {
                analyseClassFile(file, classFile, unresolvedNodes, reflectionPattern);
                File[] files = file.listFiles(ZIP_FILE_FILTER);
                for (int j = 0; j < files.length; j++) {
                    String source = createSourceName(classFile, files[j].getName());
                    analyseClassFiles(new ZipFile(files[j].getAbsoluteFile()), source,
                            unresolvedNodes, reflectionPattern);
                }
            } else if (file.getName().endsWith(".class")) {
                analyseClassFile(file, null, unresolvedNodes, reflectionPattern);
            } else if (isZipFile(file)) {
                analyseClassFiles(new ZipFile(file.getAbsoluteFile()), classFile,
                        unresolvedNodes, reflectionPattern);
            } else {
                throw new IOException(classFile + " is an invalid file.");
            }
        }
        List<UnresolvedNode> filteredNodes = new ArrayList<UnresolvedNode>();
        for (UnresolvedNode node : unresolvedNodes) {
            if (node.isMatchedBy(pattern)) {
                filteredNodes.add(node);
            }
        }
        UnresolvedNode[] nodes = new UnresolvedNode[filteredNodes.size()];
        nodes = (UnresolvedNode[]) filteredNodes.toArray(nodes);
        return nodes;
    }

}