/*
 * Copyright (c) 2003-2011, Franz-Josef Elmer, All rights reserved.
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
package classycle.ant;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import classycle.Analyser;
import classycle.dependency.DefaultResultRenderer;
import classycle.dependency.DependencyChecker;
import classycle.dependency.ResultRenderer;
import classycle.util.Text;

/**
 * Ant Task for checking class dependencies.
 * <p>
 * <table border="1" cellpadding="5" cellspacing="0">
 * <tr><th>Attribute</th><th>Description</th><th>Required</th></tr>
 * <tr><td valign="top">includingClasses</td>
 * <td>Comma or space separated list of wild-card patterns of
 * fully-qualified class name which are included in the analysis.
 * Only '*' are recognized as wild-card character.
 * </td>
 * <td valign="top">No. By default all classes defined in the file set
 * are included.
 * </td>
 * </tr>
 * <tr><td valign="top">excludingClasses</td>
 * <td valign="top">Comma or space separated list of wild-card patterns of
 * fully-qualified class name which are excluded from the analysis.
 * Only '*' are recognized as wild-card character.
 * </td>
 * <td valign="top">No. By default no class defined in the file set is
 * excluded.
 * </td>
 * </tr>
 * <tr><td valign="top">mergeInnerClasses</td>
 * <td valign="top">If <code>true</code> all class vertices are merged
 * with the vertices of the corresponding inner classes.
 * </td>
 * <td valign="top">No. Default is <tt>false</tt>.</td>
 * </tr>
 * <tr><td valign="top">reflectionPattern</td>
 * <td valign="top">Comma or space separated list of wild-card patterns of
 * fully-qualified class name.
 * Only '*' are recognized as wild-card character.
 * <p>
 * If in the code of a class an ordinary string constant matches
 * one of these patterns and if this string constant
 * has a valid syntax for a fully-qualified
 * class name this constant will be treated as a class reference.
 * </td>
 * <td valign="top">No. By default ordinary string constants are not
 * treated as class references.
 * </td>
 * </tr>
 * <tr><td valign="top">definitionFile</td>
 * <td valign="top">Path of the dependency definition file.
 * It is either absolute or relative to the base directory.
 * </td>
 * <td valign="top">No. By default the dependency definition commands
 * are embedded in the ant task.
 * </td>
 * </tr>
 * <tr><td valign="top">failOnUnwantedDependencies</td>
 * <td valign="top">If <tt>true</tt> the task will fail if an
 * unwanted dependency is found.
 * </td>
 * <td valign="top">No. Default value is <tt>false</tt>.
 * </td>
 * </tr>
 * <tr><td valign="top">reportFile</td>
 * <td valign="top">Path of the report file.
 * It is either absolute or relative to the base directory.</td>
 * <td valign="top">No. By default the result is written onto the console.</td>
 * <tr><td valign="top">resultRenderer</td>
 * <td valign="top">Fully-qualified class name of a
 * {@link ResultRenderer}.
 * </td>
 * <td valign="top">No. By default {@link DefaultResultRenderer} is used.
 * </td>
 * </tr>
 * </table>
 *
 * @author Franz-Josef Elmer
 */
public class DependencyCheckingTask extends ClassycleTask {
    private File _definitionFile;
    private String _dependencyDefinition;
    private String _resultRenderer;
    private boolean _failOnUnwantedDependencies;

    public void setFailOnUnwantedDependencies(boolean failOnUnwantedDependencies) {
        _failOnUnwantedDependencies = failOnUnwantedDependencies;
    }

    public void setDefinitionFile(File definitionFile) {
        _definitionFile = definitionFile;
    }

    public void setResultRenderer(String resultRenderer) {
        _resultRenderer = resultRenderer;
    }

    public void addText(String text) {
        _dependencyDefinition = text.trim();
    }

    @SuppressWarnings("unchecked")
    public void execute() throws BuildException {
        super.execute();

        boolean ok = false;
        PrintWriter printWriter = null;
        try {
            Analyser analyser = new Analyser(getClassFileNames(), getPattern(),
                    getReflectionPattern(),
                    isMergeInnerClasses());
            Map<Object, Object> properties = _definitionFile == null ? (Map) getProject().getProperties() : System.getProperties();
            DependencyChecker dependencyChecker = new DependencyChecker(analyser, getDependencyDefinitions(),
                    properties,
                    getRenderer());
            printWriter = _reportFile == null ? new PrintWriter(System.out)
                    : new PrintWriter(new FileWriter(_reportFile));
            ok = dependencyChecker.check(printWriter);
            printWriter.flush();
            printWriter.close();
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
        if (_failOnUnwantedDependencies && !ok) {
            throw new BuildException(
                    "Unwanted dependencies found. See output for details.");
        }
    }

    private ResultRenderer getRenderer() throws InstantiationException,
            IllegalAccessException,
            ClassNotFoundException {
        ResultRenderer renderer = new DefaultResultRenderer();
        if (_resultRenderer != null) {
            renderer = (ResultRenderer) Class.forName(_resultRenderer).newInstance();
        }
        return renderer;
    }

    private String getDependencyDefinitions() throws IOException, BuildException {
        String result = _dependencyDefinition;
        ;
        if (_definitionFile != null) {
            result = Text.readTextFile(_definitionFile);
        }
        if (result.length() == 0) {
            throw new BuildException("Empty dependency definition.");
        }
        return result;
    }
}
