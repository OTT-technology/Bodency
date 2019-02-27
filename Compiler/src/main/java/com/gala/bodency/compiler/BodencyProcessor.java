package com.gala.bodency.compiler;

import com.gala.bodency.annotation.BodencyAnnotation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class BodencyProcessor extends AbstractProcessor {

    private static final String PACKAGE_NAME = "com.gala.bodency.annotation";
    private static final String CLASS_NAME_BUILD_PROVIDER = "AutoOutputClass";

    public static final int CLASS_COUNT = 325;
    public static final int METHOD_COUNT = 200;

    // 文件相关的辅助类
    private Filer mFiler;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
        addMethod();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(BodencyAnnotation.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void addMethod() {
        for (int i = 0; i < CLASS_COUNT; i++) {
            TypeSpec.Builder builder = TypeSpec.classBuilder(CLASS_NAME_BUILD_PROVIDER + i).addModifiers(Modifier.PUBLIC);
            for (int j = 0; j < METHOD_COUNT; j++) {
                MethodSpec ms = MethodSpec.methodBuilder("auto" + j)
                        .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                        .addStatement("android.util.Log.i(\"" + CLASS_NAME_BUILD_PROVIDER + i + "\", \"method " + j + "\");")
                        .build();
                builder.addMethod(ms);
            }
            try {
                JavaFile.builder(PACKAGE_NAME, builder.build()).build().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void p(String log) {
        mMessager.printMessage(Diagnostic.Kind.OTHER, log);
    }
}
