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
package classycle.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sequence of {@link StringPattern StringPatterns}.
 *
 * @author Franz-Josef Elmer
 */
public abstract class StringPatternSequence implements StringPattern {
    protected final List<StringPattern> _patterns = new ArrayList<StringPattern>();

    protected StringPatternSequence(StringPattern[] pattern) {
        _patterns.addAll(Arrays.asList(pattern));
    }

    /**
     * Appends the specified pattern.
     */
    public void appendPattern(StringPattern pattern) {
        _patterns.add(pattern);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        int size = _patterns.size();
        String operatorSymbol = getOperatorSymbol();
        boolean bracketsNeeded = size > 1 && operatorSymbol.equals(" & ");
        if (bracketsNeeded) {
            buffer.append('(');
        }
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                buffer.append(operatorSymbol);
            }
            buffer.append(_patterns.get(i));
        }
        return new String(bracketsNeeded ? buffer.append(')') : buffer);
    }

    /**
     * Returns the operator symbol for pretty printing. Needed by
     * <code>toString()</code>.
     */
    protected abstract String getOperatorSymbol();

}
