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
package classycle.classfile;

/**
 * Constant holding a name and a type or method descriptor.
 *
 * @author Franz-Josef Elmer
 */
public class NameAndTypeConstant extends Constant {
    private final int _nameIndex;
    private final int _descriptorIndex;

    /**
     * Creates an instance for the specified name and type or method descriptor.
     *
     * @param pool            Constant pool. Needed to resolve references.
     * @param nameIndex       Index of the name in the pool.
     * @param descriptorIndex Index of the type or method descriptor in the pool.
     */
    public NameAndTypeConstant(Constant[] pool, int nameIndex,
                               int descriptorIndex) {
        super(pool);
        _nameIndex = nameIndex;
        _descriptorIndex = descriptorIndex;
    }

    /**
     * Returns the name.
     */
    public String getName() {
        String result = null;
        Constant c = getConstant(_nameIndex);
        if (c instanceof UTF8Constant) {
            result = ((UTF8Constant) c).getString();
        }
        return result;
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
     * Returns constant type, name, and descriptor.
     */
    public String toString() {
        return "CONSTANT_NameAndType: " + getName() + ", " + getDescriptor();
    }
} //class