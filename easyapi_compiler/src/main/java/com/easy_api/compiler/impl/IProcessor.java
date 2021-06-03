package com.easy_api.compiler.impl;


import com.easy_api.compiler.AnnotationProcessor;

import javax.annotation.processing.RoundEnvironment;

/**
 * Created by liweihua on 2018/12/18.
 */

public interface IProcessor {
    void process(RoundEnvironment roundEnv, AnnotationProcessor annotationProcessor);
}
