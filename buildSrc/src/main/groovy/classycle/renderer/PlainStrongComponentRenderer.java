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
package classycle.renderer;

import classycle.graph.StrongComponent;

/**
 * Plain text renderer of a {@link StrongComponent}.
 *
 * @author Franz-Josef Elmer
 */
public class PlainStrongComponentRenderer
        extends AbstractStrongComponentRenderer {
    public String render(StrongComponent component) {
        AtomicVertexRenderer classRenderer = new PlainClassRenderer();
        StringBuffer result = new StringBuffer();
        int n = component.getNumberOfVertices();
        if (n == 1) {
            result.append(classRenderer.render(component.getVertex(0), null, 0))
                    .append(". Layer: ").append(component.getLongestWalk());
        } else {
            result.append("Cycle: ").append(createName(component)).append(" with ")
                    .append(n).append(" vertices.")
                    .append(" Layer: ").append(component.getLongestWalk());
            for (int i = 0; i < n; i++) {
                result.append("\n    ")
                        .append(classRenderer.render(component.getVertex(i), null, 0));
            }
        }
        return new String(result);
    }
} //class