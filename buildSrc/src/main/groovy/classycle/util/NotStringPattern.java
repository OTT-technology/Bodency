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

/**
 * Logical NOT operation on the result of a wrapped {@link StringPattern}.
 *
 * @author Franz-Josef Elmer
 */
public class NotStringPattern implements StringPattern {
    private final StringPattern _pattern;

    /**
     * Creates an instance based on the specified pattern.
     *
     * @param pattern Pattern who's matching result will be negate.
     *                Must be not <tt>null</tt>.
     */
    public NotStringPattern(StringPattern pattern) {
        _pattern = pattern;
    }

    /**
     * Returns <tt>true</tt> if the wrapped {@link StringPattern} returns
     * <tt>false</tt> and vice-versa.
     */
    public boolean matches(String string) {
        return !_pattern.matches(string);
    }

    public String toString() {
        String expression = _pattern.toString();
        boolean bracketsNeeded = expression.startsWith("(") == false && expression.indexOf(' ') > 0;
        return '!' + (bracketsNeeded ? '(' + expression + ')' : expression);
    }
}
