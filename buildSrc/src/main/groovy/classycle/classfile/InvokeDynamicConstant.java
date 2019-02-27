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
 * Constant specifying a bootstrap method..
 *
 * @author Franz-Josef Elmer
 */
public class InvokeDynamicConstant extends Constant {

    private final int _bootstrapMethodAttrIndex;
    private final int _nameAndTypeIndex;

    /**
     * Creates an instance for the specified bootstrap method.
     *
     * @param pool Constant pool. Needed to resolve references.
     */
    public InvokeDynamicConstant(Constant[] pool, int bootstrapMethodAttrIndex, int nameAndTypeIndex) {
        super(pool);
        _bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
        _nameAndTypeIndex = nameAndTypeIndex;
    }

    /**
     * Returns name and type.
     */
    public String getNameAndType() {
        return String.valueOf(getConstant(_nameAndTypeIndex));
    }

    /**
     * Returns constant type and descriptor.
     */
    public String toString() {
        return "CONSTANT_InvokeDynamic: " + _bootstrapMethodAttrIndex + " [" + getNameAndType() + "]";
    }
} //class