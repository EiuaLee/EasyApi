package com.easy_api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * liweihua
 * 2019/4/15 下午1:51
 * 忽略错误预处理
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface EasyErrorHandle {
    boolean ignore() default true;
}
