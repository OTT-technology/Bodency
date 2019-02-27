/*
 * Copyright (c) 2014, Franz-Josef Elmer, All rights reserved.
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
package classycle.classfile;

/**
 * Constant representing a method type.
 *
 * @author Franz-Josef Elmer
 */
public class MethodTypeConstant extends Constant {
    private final int _descriptorIndex;

    /**
     * Creates an instance for the specified method reference.
     *
     * @param pool Constant pool. Needed to resolve references.
     */
    public MethodTypeConstant(Constant[] pool, int descriptorIndex) {
        super(pool);
        _descriptorIndex = descriptorIndex;
    }

    /**
     * Returns the type or method descriptor.
     */
    public String getDescriptor() {
        String result = null;
        Constant c = getConstant(_descriptorIndex);
        if (c instanceof UTF8Constant) {
            result = ((UTF8Constant) c).getString();
        }
        return result;
    }

    /**
     * Returns constant type and descriptor.
     */
    public String toString() {
        return "CONSTANT_MethodType: " + getDescriptor();
    }
} //class