package com.easy_api.compiler;

import com.google.auto.service.AutoService;
import com.easy_api.compiler.processor.ApiRepoertoryProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)//java版本支持
@SupportedAnnotationTypes(
        {
                "com.easy_api.annotation.ApiFactory"
        }
)//标注注解处理器支持的注解类型
public class AnnotationProcessor extends AbstractProcessor {
    public Filer mFiler; //文件相关的辅助类
    public Elements mElements; //元素相关的辅助类
    public Types mTypes; //类型相关辅助类
    public Messager mMessager; //日志相关的辅助类

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        mTypes = processingEnv.getTypeUtils();

        new ApiRepoertoryProcessor().process(roundEnv, this);
        return true;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    /**
     * 获取TypeName实体
     * @param elementName
     * @return
     */
    public TypeName getTypeName(String elementName) {
        return ClassName.get(mElements.getTypeElement(elementName).asType());
    }

    public void log(String string) {
        mMessager.printMessage(Diagnostic.Kind.WARNING, string);
    }
}


