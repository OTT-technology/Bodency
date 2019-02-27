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
/*
 * Created on 07.04.2006
 */
package classycle.dependency;

import classycle.graph.AtomicVertex;
import classycle.graph.NameAttributes;
import classycle.util.StringPattern;

public class CheckSetStatement implements Statement {
    private final StringPattern _set;
    private final SetDefinitionRepository _repository;

    public CheckSetStatement(StringPattern set,
                             SetDefinitionRepository repository) {
        _set = set;
        _repository = repository;
    }

    public Result execute(AtomicVertex[] graph) {
        int size = 0;
        for (int i = 0; i < graph.length; i++) {
            if (_set.matches(((NameAttributes) graph[i].getAttributes()).getName())) {
                size++;
            }
        }
        StringBuffer buffer = new StringBuffer("Set ");
        buffer.append(_repository.toString(_set));
        if (size == 0) {
            buffer.append(" is empty.");
        } else if (size == 1) {
            buffer.append(" has one class.");
        } else {
            buffer.append(" has ").append(size).append(" classes.");
        }
        return new TextResult(new String(buffer.append('\n')), size > 0);
    }

    public String toString() {
        return DependencyDefinitionParser.CHECK_KEY_WORD + " set "
                + _repository.toString(_set);
    }


}
