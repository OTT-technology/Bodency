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
package classycle.dependency;

import com.gala.bodency.utils.Logger;

import classycle.graph.AtomicVertex;

/**
 * Processor of {@link Statement Statements} as defined in a
 * dependency definition file.
 *
 * @author Franz-Josef Elmer
 */
public class DependencyProcessor {
    private static final String TAG = "DependencyProcessor";
    private final Statement[] _statements;
    private int _index;

    /**
     * Creates a new instance for the specified dependency definition. It also
     * parses the definition and creates all {@link Statement Statements}.
     *
     * @param dependencyDefinition Dependency definition as read from a
     *                             .ddf file.
     * @param properties           Contains predefined properties and will also
     *                             be populated by definition in <code>dependencyDefinition</code>.
     * @param renderer             Renderer for processing results.
     * @throws IllegalArgumentException if <tt>dependencyDefinition</tt>
     *                                  is invalid.
     */
    public DependencyProcessor(String dependencyDefinition,
                               DependencyProperties properties,
                               ResultRenderer renderer) {
        _statements = new DependencyDefinitionParser(dependencyDefinition, properties, renderer).getStatements();
    }

    /**
     * Returns <tt>true</tt> if there are still unprocessed statements.
     */
    public boolean hasMoreStatements() {
        return _index < _statements.length;
    }

    /**
     * Executes the next unprocessed statement and returns its result.
     *
     * @param graph The graph to be checked by the statement.
     * @return <tt>null</tt> if there is no unprocessed statement.
     */
    public Result executeNextStatement(AtomicVertex[] graph) {
        Logger.p(TAG, "executeNextStatement -> " + _statements[_index].getClass());
        return hasMoreStatements() ? _statements[_index++].execute(graph) : null;
    }
}
