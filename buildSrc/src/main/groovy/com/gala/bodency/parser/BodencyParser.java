package com.gala.bodency.parser;

import com.gala.bodency.utils.Logger;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import classycle.ClassAttributes;
import classycle.ClassNameExtractor;
import classycle.UnresolvedNode;
import classycle.classfile.ClassConstant;
import classycle.classfile.Constant;
import classycle.classfile.StringConstant;
import classycle.classfile.UTF8Constant;
import classycle.util.StringPattern;
import classycle.util.TrueStringPattern;

public class BodencyParser {
    public static final String TAG = "BodencyParser";

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
    public BodencyParser() {
    }

    /**
     * Reads and parses class files and creates a direct graph. Short-cut of
     * <tt>readClassFiles(classFiles, new {@link TrueStringPattern}(),
     * null, false);</tt>
     */
    public HashMap<String, UnresolvedNode> readClassFiles(String[] classFiles) throws IOException {
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
    public HashMap<String, UnresolvedNode> readClassFiles(String[] classFiles, StringPattern pattern,
                                                          StringPattern reflectionPattern, boolean mergeInnerClasses)
            throws IOException {
        HashMap<String, UnresolvedNode> unresolvedNodes = new HashMap<>();
        for (int i = 0; i < classFiles.length; i++) {
            unresolvedNodes.putAll(readClassFiles(classFiles[i], reflectionPattern));
        }
//        ArrayList<UnresolvedNode> filteredNodes = new ArrayList<UnresolvedNode>();
//        for (UnresolvedNode node : unresolvedNodes) {
//            if (node.isMatchedBy(pattern)) {
//                filteredNodes.add(node);
//            }
//        }
        return unresolvedNodes;
    }

    public HashMap<String, UnresolvedNode> readClassFiles(String classFile, StringPattern reflectionPattern) throws IOException {
        Map<String, ZipFile> archives = new HashMap<String, ZipFile>();
        HashMap<String, UnresolvedNode> unresolvedNodes = null;
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
            unresolvedNodes = analyseClassFileFromZipEntry(zipFile, entry, archivePath, reflectionPattern);
            return unresolvedNodes;
        }
        File file = new File(classFile);
        if (file.isDirectory()) {
            unresolvedNodes = analyseClassFile(file, classFile, reflectionPattern);
            File[] files = file.listFiles(ZIP_FILE_FILTER);
            for (int j = 0; j < files.length; j++) {
                String source = createSourceName(classFile, files[j].getName());
                analyseClassFiles(new ZipFile(files[j].getAbsoluteFile()), source, reflectionPattern);
            }
        } else if (file.getName().endsWith(".class")) {
            unresolvedNodes = analyseClassFile(file, null, reflectionPattern);
        } else if (isZipFile(file)) {
            unresolvedNodes = analyseClassFiles(new ZipFile(file.getAbsoluteFile()), classFile, reflectionPattern);
        } else {
            Logger.p(TAG, classFile + " is an invalid file.");
        }
        return unresolvedNodes;
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

    private HashMap<String, UnresolvedNode> analyseClassFile(File file, String source, StringPattern reflectionPattern) throws IOException {
        HashMap<String, UnresolvedNode> unresolvedNodes = new HashMap<>();
        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                File child = new File(file, files[i]);
                if (child.isDirectory() || files[i].endsWith(".class")) {
                    unresolvedNodes.putAll(analyseClassFile(child, source, reflectionPattern));
                }
            }
        } else {
            UnresolvedNode unresolvedNode = extractNode(file, source, reflectionPattern);
            unresolvedNodes.put(unresolvedNode.getAttributes().getName(), unresolvedNode);
        }
        return unresolvedNodes;
    }

    private UnresolvedNode extractNode(File file, String source, StringPattern reflectionPattern)
            throws IOException {
        InputStream stream = null;
        UnresolvedNode result = null;
        try {
            stream = new FileInputStream(file);
            result = createNode(stream, source, (int) file.length(),
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

    private HashMap<String, UnresolvedNode> analyseClassFiles(ZipFile zipFile, String source, StringPattern reflectionPattern)
            throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        HashMap<String, UnresolvedNode> unresolvedNodes = new HashMap<>();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            unresolvedNodes.putAll(analyseClassFileFromZipEntry(zipFile, entry, source, reflectionPattern));
        }
        return unresolvedNodes;
    }

    private HashMap<String, UnresolvedNode> analyseClassFileFromZipEntry(ZipFile zipFile, ZipEntry entry, String source, StringPattern reflectionPattern)
            throws IOException {
        HashMap<String, UnresolvedNode> unresolvedNodes = new HashMap<>();
        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
            InputStream stream = zipFile.getInputStream(entry);
            int size = (int) entry.getSize();
            UnresolvedNode node = createNode(stream, source, size, reflectionPattern);
            unresolvedNodes.put(node.getAttributes().getName(), node);
        }
        return unresolvedNodes;
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
    private static UnresolvedNode createNode(InputStream stream, String source, int size,
                                             StringPattern reflectionPattern)
            throws IOException {

        // Reads constant pool, accessFlags, and class name
        DataInputStream dataStream = new DataInputStream(stream);
        UnresolvedNode node = UnresolvedNode.extractConstantPool(dataStream);
        int accessFlags = dataStream.readUnsignedShort();
        String name = ((ClassConstant) node.mPool[dataStream.readUnsignedShort()]).getName();
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
        node.setAttributes(attributes);
        for (int i = 0; i < node.mPool.length; i++) {
            Constant constant = node.mPool[i];
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
}
