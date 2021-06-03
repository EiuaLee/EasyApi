package com.easy_api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ApiFactory {

    //Api实现文件生成路径
    String createApiFilePath();

    //生成的Api实现文件名称
    String createApiFileName() default "ApiRepoertory";

    //api实现类的路径
    String api();

    //对于返回值为T的实现
    String baseResp();

    //对于返回值为List<T>的实现
    String baseListResp();

    //是否全局自动刷新Token
    boolean isEnableAutoRefreshToken() default true;

    //是否全局自动处理错误信息
    boolean isEnableAutoHandleError() default true;
}

