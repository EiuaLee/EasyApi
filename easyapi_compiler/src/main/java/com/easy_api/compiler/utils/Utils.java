package com.easy_api.compiler.utils;

import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Created by liweihua on 2018/12/18.
 */

public class Utils {

    public static final String ANNOTATION = "@";

    public static boolean isPublic(TypeElement element) {
        return element.getModifiers().contains(PUBLIC);
    }

    public static boolean isAbstract(TypeElement element) {
        return element.getModifiers().contains(ABSTRACT);
    }

    public static boolean isValidClass(Messager messager, TypeElement element) {
        if (element.getKind() != ElementKind.CLASS) {
            return false;
        }

        if (!isPublic(element)) {
            String message = String.format("Classes annotated with %s must be public.", ANNOTATION);
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
            return false;
        }

        if (isAbstract(element)) {
            String message = String.format("Classes annotated with %s must not be abstract.", ANNOTATION);
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
            return false;
        }

        return true;
    }



    /**
     * 获取泛型String
     * @param indexStr 开始获取的位置
     * @param string 原始数据
     * @return 泛型 String
     */
    public static String getGenericParadigm(String string, String indexStr) {
        String replaceStr = string.replace(indexStr, "");
        if (replaceStr.lastIndexOf(">") < 0) {
            return string;
        }
        String paramsGenericParadigm = replaceStr.substring(0, replaceStr.lastIndexOf(">"));
        return paramsGenericParadigm;
    }

}
