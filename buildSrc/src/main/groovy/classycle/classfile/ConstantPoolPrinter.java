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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Application which prints the {@link Constant} pool of a class file onto the
 * console.
 *
 * @author Franz-Josef Elmer
 */
public class ConstantPoolPrinter {
    /**
     * Reads a class file and prints the constant pool onto <tt>System.out</tt>.
     *
     * @param args File name of the class file in <tt>args[0]</tt>
     */
    public static void main(String[] args) {
        classAnalyser(args);
    }

    public static Map<String, Constant[]> classAnalyser(String[] filenames) {
        if (filenames.length == 0) {
            System.out.println("Usage: java classycle.classfile.ConstantPoolPrinter <class files>");
        }
        Map<String, Constant[]> result = new HashMap<>();
        for (String filename : filenames) {
            Constant[] pool = investigate(filename);
            result.put(filename, pool);
        }
        return result;
    }

    private static Constant[] investigate(String fileName) {
        DataInputStream stream = null;
        try {
            stream = new DataInputStream(new FileInputStream(fileName));
            Constant[] pool = Constant.extractConstantPool(stream);
            printConstantPool(pool);
            return pool;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void printConstantPool(Constant[] pool) {
        for (int i = 0; i < pool.length; i++) {
            System.out.println(i + ": " + pool[i]);
        }
    }
} //class