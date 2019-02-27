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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Abstract super class of all type of constants in the constant pool of
 * a class file.
 *
 * @author Franz-Josef Elmer
 */
public abstract class Constant {
    public static final int MAGIC = 0xcafebabe;
    public static final int CONSTANT_CLASS = 7,
            CONSTANT_FIELDREF = 9,
            CONSTANT_METHODREF = 10,
            CONSTANT_INTERFACE_METHODREF = 11,
            CONSTANT_STRING = 8,
            CONSTANT_INTEGER = 3,
            CONSTANT_FLOAT = 4,
            CONSTANT_LONG = 5,
            CONSTANT_DOUBLE = 6,
            CONSTANT_NAME_AND_TYPE = 12,
            CONSTANT_UTF8 = 1,
            CONSTANT_METHOD_HANDLE = 15,
            CONSTANT_METHOD_TYPE = 16,
            CONSTANT_INVOKE_DYNAMIC = 18;

    /**
     * Extracts the constant pool from the specified data stream of a class file.
     *
     * @param stream Input stream of a class file starting at the first byte.
     * @return extracted array of constants.
     * @throws IOException in case of reading errors or invalid class file.
     */
    public static Constant[] extractConstantPool(DataInputStream stream)
            throws IOException {
        Constant[] pool = null;
        if (stream.readInt() == MAGIC) {
            stream.readUnsignedShort();
            stream.readUnsignedShort();
            pool = new Constant[stream.readUnsignedShort()];
            for (int i = 1; i < pool.length; ) {
                boolean skipIndex = false;
                Constant c = null;
                int type = stream.readUnsignedByte();
                switch (type) {
                    case CONSTANT_CLASS:
                        c = new ClassConstant(pool, stream.readUnsignedShort());
                        break;
                    case CONSTANT_FIELDREF:
                        c = new FieldRefConstant(pool, stream.readUnsignedShort(),
                                stream.readUnsignedShort());
                        break;
                    case CONSTANT_METHODREF:
                        c = new MethodRefConstant(pool, stream.readUnsignedShort(),
                                stream.readUnsignedShort());
                        break;
                    case CONSTANT_INTERFACE_METHODREF:
                        c = new InterfaceMethodRefConstant(pool,
                                stream.readUnsignedShort(),
                                stream.readUnsignedShort());
                        break;
                    case CONSTANT_STRING:
                        c = new StringConstant(pool, stream.readUnsignedShort());
                        break;
                    case CONSTANT_INTEGER:
                        c = new IntConstant(pool, stream.readInt());
                        break;
                    case CONSTANT_FLOAT:
                        c = new FloatConstant(pool, stream.readFloat());
                        break;
                    case CONSTANT_LONG:
                        c = new LongConstant(pool, stream.readLong());
                        skipIndex = true;
                        break;
                    case CONSTANT_DOUBLE:
                        c = new DoubleConstant(pool, stream.readDouble());
                        skipIndex = true;
                        break;
                    case CONSTANT_NAME_AND_TYPE:
                        c = new NameAndTypeConstant(pool, stream.readUnsignedShort(),
                                stream.readUnsignedShort());
                        break;
                    case CONSTANT_UTF8:
                        c = new UTF8Constant(pool, stream.readUTF());
                        break;
                    case CONSTANT_METHOD_HANDLE:
                        c = new MethodHandleConstant(pool, stream.readUnsignedByte(), stream.readUnsignedShort());
                        break;
                    case CONSTANT_METHOD_TYPE:
                        c = new MethodTypeConstant(pool, stream.readUnsignedShort());
                        break;
                    case CONSTANT_INVOKE_DYNAMIC:
                        c = new InvokeDynamicConstant(pool, stream.readUnsignedShort(), stream.readUnsignedShort());
                        break;
                }
                pool[i] = c;
                i += skipIndex ? 2 : 1; // double and long constants occupy two entries
            }
            return pool;
        }
        throw new IOException("Not a class file: Magic number missing.");
    }

    private Constant[] _pool;

    /**
     * Creates an instance.
     *
     * @param pool The poole which will be needed to resolve references.
     */
    public Constant(Constant[] pool) {
        _pool = pool;
    }

    /**
     * Returns the specified constant from the pool.
     *
     * @param index Index of requested constant.
     */
    public Constant getConstant(int index) {
        return _pool[index];
    }
}