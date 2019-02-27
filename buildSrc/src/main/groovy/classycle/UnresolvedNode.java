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
package classycle;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import classycle.classfile.ClassConstant;
import classycle.classfile.Constant;
import classycle.classfile.DoubleConstant;
import classycle.classfile.FieldRefConstant;
import classycle.classfile.FloatConstant;
import classycle.classfile.IntConstant;
import classycle.classfile.InterfaceMethodRefConstant;
import classycle.classfile.InvokeDynamicConstant;
import classycle.classfile.LongConstant;
import classycle.classfile.MethodHandleConstant;
import classycle.classfile.MethodRefConstant;
import classycle.classfile.MethodTypeConstant;
import classycle.classfile.NameAndTypeConstant;
import classycle.classfile.StringConstant;
import classycle.classfile.UTF8Constant;
import classycle.util.StringPattern;

/**
 * Class representing a node without resolved links.
 *
 * @author Franz-Josef Elmer
 */
public class UnresolvedNode implements Comparable<UnresolvedNode> {

    private ClassAttributes _attributes;
    private List<String> _nodes = new ArrayList<String>();
    public Constant[] mPool;
    public int mClassCount = 0;
    public int mFieldRefCount = 0;
    public int mMethodRefCount = 0;
    public int mInterfaceMethodRefCount = 0;
    public int mStringCount = 0;
    public int mIntCount = 0;
    public int mFloatCount = 0;
    public int mLongCount = 0;
    public int mDoubleCount = 0;
    public int mNameAndTypeCount = 0;
    public int mUTF8Count = 0;
    public int mMethodHandleCount = 0;
    public int mMethodTypeCount = 0;
    public int mInvokeDynamicCount = 0;

    public void setAttributes(ClassAttributes attributes) {
        _attributes = attributes;
    }

    public ClassAttributes getAttributes() {
        return _attributes;
    }

    public void addLinkTo(String node) {
        _nodes.add(node);
    }

    public Iterator<String> linkIterator() {
        return new Iterator<String>() {
            private int _index;

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return _index < _nodes.size();
            }

            public String next() {
                return hasNext() ? _nodes.get(_index++) : null;
            }
        };
    }

    public int compareTo(UnresolvedNode obj) {
        return getAttributes().getName().compareTo(obj.getAttributes().getName());
    }

    public boolean isMatchedBy(StringPattern pattern) {
        return pattern.matches(getAttributes().getName());
    }

    public String getName() {
        return _attributes.getName();
    }

    public String getType() {
        return _attributes.getType();
    }

    /**
     * Extracts the constant pool from the specified data stream of a class file.
     *
     * @param stream Input stream of a class file starting at the first byte.
     * @return extracted array of constants.
     * @throws IOException in case of reading errors or invalid class file.
     */
    public static UnresolvedNode extractConstantPool(DataInputStream stream) throws IOException {
        UnresolvedNode node = new UnresolvedNode();
        Constant[] pool = null;
        if (stream.readInt() == Constant.MAGIC) {
            stream.readUnsignedShort();
            stream.readUnsignedShort();
            pool = new Constant[stream.readUnsignedShort()];
            for (int i = 1; i < pool.length; ) {
                boolean skipIndex = false;
                Constant c = null;
                int type = stream.readUnsignedByte();
                switch (type) {
                    case Constant.CONSTANT_CLASS:
                        c = new ClassConstant(pool, stream.readUnsignedShort());
                        node.mClassCount++;
                        break;
                    case Constant.CONSTANT_FIELDREF:
                        c = new FieldRefConstant(pool, stream.readUnsignedShort(),
                                stream.readUnsignedShort());
                        node.mFieldRefCount++;
                        break;
                    case Constant.CONSTANT_METHODREF:
                        c = new MethodRefConstant(pool, stream.readUnsignedShort(),
                                stream.readUnsignedShort());
                        node.mMethodRefCount++;
                        break;
                    case Constant.CONSTANT_INTERFACE_METHODREF:
                        c = new InterfaceMethodRefConstant(pool,
                                stream.readUnsignedShort(),
                                stream.readUnsignedShort());
                        node.mInterfaceMethodRefCount++;
                        break;
                    case Constant.CONSTANT_STRING:
                        c = new StringConstant(pool, stream.readUnsignedShort());
                        node.mStringCount++;
                        break;
                    case Constant.CONSTANT_INTEGER:
                        c = new IntConstant(pool, stream.readInt());
                        node.mIntCount++;
                        break;
                    case Constant.CONSTANT_FLOAT:
                        c = new FloatConstant(pool, stream.readFloat());
                        node.mFloatCount++;
                        break;
                    case Constant.CONSTANT_LONG:
                        c = new LongConstant(pool, stream.readLong());
                        skipIndex = true;
                        node.mLongCount++;
                        break;
                    case Constant.CONSTANT_DOUBLE:
                        c = new DoubleConstant(pool, stream.readDouble());
                        skipIndex = true;
                        node.mDoubleCount++;
                        break;
                    case Constant.CONSTANT_NAME_AND_TYPE:
                        c = new NameAndTypeConstant(pool, stream.readUnsignedShort(),
                                stream.readUnsignedShort());
                        node.mNameAndTypeCount++;
                        break;
                    case Constant.CONSTANT_UTF8:
                        c = new UTF8Constant(pool, stream.readUTF());
                        node.mUTF8Count++;
                        break;
                    case Constant.CONSTANT_METHOD_HANDLE:
                        c = new MethodHandleConstant(pool, stream.readUnsignedByte(), stream.readUnsignedShort());
                        node.mMethodHandleCount++;
                        break;
                    case Constant.CONSTANT_METHOD_TYPE:
                        c = new MethodTypeConstant(pool, stream.readUnsignedShort());
                        node.mMethodTypeCount++;
                        break;
                    case Constant.CONSTANT_INVOKE_DYNAMIC:
                        c = new InvokeDynamicConstant(pool, stream.readUnsignedShort(), stream.readUnsignedShort());
                        node.mInvokeDynamicCount++;
                        break;
                }
                pool[i] = c;
                i += skipIndex ? 2 : 1; // double and long constants occupy two entries
            }
            node.mPool = pool;
            return node;
        }
        throw new IOException("Not a class file: Magic number missing.");
    }
}