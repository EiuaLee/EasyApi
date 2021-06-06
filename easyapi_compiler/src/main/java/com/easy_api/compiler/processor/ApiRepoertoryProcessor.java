package com.easy_api.compiler.processor;

import com.easy_api.annotation.ApiFactory;
import com.easy_api.annotation.EasyErrorHandle;
import com.easy_api.annotation.EasyRefreshToken;
import com.easy_api.compiler.AnnotationProcessor;
import com.easy_api.compiler.impl.IProcessor;
import com.easy_api.compiler.utils.C;
import com.easy_api.compiler.utils.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static com.squareup.javapoet.TypeSpec.classBuilder;

public class ApiRepoertoryProcessor implements IProcessor {

    private AnnotationProcessor annotationProcessor;
    private RoundEnvironment roundEnv;

    private final String easyApiPath = "com.eiualee.easyapi.EasyApi";//EasyApi位置

    private String baseListRespPath = ""; //baseListResp 路径
    private String baseRespPath = ""; //baseListResp 路径
    private String apiPath = ""; //api 路径
    private String apiRepoertoryPath = ""; //api实现类 路径
    private boolean isEnableAutoRefreshToken; //是否自动添加Token刷新
    private boolean isEnableAutoHandleError; //是否自动处理错误
    private String[] paramsReplaceKey; //细粒度的替换集合
    private String[] paramsReplaceValue; //细粒度的替换集合


    @Override
    public void process(RoundEnvironment roundEnv, AnnotationProcessor annotationProcessor) {

        this.annotationProcessor = annotationProcessor;
        this.roundEnv = roundEnv;


        //先获取有ApiFactory 注解的类
        for (Element element : roundEnv.getElementsAnnotatedWith(ApiFactory.class)) {

            ApiFactory apiFactory = element.getAnnotation(ApiFactory.class);


            TypeSpec.Builder classbuilder = classBuilder(apiFactory.createApiFileName())
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addJavadoc("@ Api请求实现,由Apt自动生成,在此上添加代码无效");

            baseListRespPath = apiFactory.baseListResp();
            baseRespPath = apiFactory.baseResp();
            apiPath = apiFactory.api();
            apiRepoertoryPath = apiFactory.createApiFilePath();
            isEnableAutoRefreshToken = apiFactory.isEnableAutoRefreshToken();
            isEnableAutoHandleError = apiFactory.isEnableAutoHandleError();
            paramsReplaceKey = apiFactory.paramsReplaceKey();
            paramsReplaceValue = apiFactory.paramsReplaceValue();

            //遍历有ApiFactory 注解的类，获取这个类的 方法对象
            for (Element e : element.getEnclosedElements()) {
                ExecutableElement executableElement = (ExecutableElement) e;
                TypeMirror elementReturnType = executableElement.getReturnType();

                boolean isIgnoreRrefreshToken = !isEnableAutoRefreshToken;
                boolean isIgnoreHandleError = !isEnableAutoHandleError;
                EasyRefreshToken easyRefreshToken = e.getAnnotation(EasyRefreshToken.class);
                if (easyRefreshToken != null) {
                    isIgnoreRrefreshToken = easyRefreshToken.ignore();
                }

                EasyErrorHandle easyErrorHandle = e.getAnnotation(EasyErrorHandle.class);
                if (easyErrorHandle != null) {
                    isIgnoreHandleError = easyErrorHandle.ignore();
                }

                MethodSpec methodSpec = null;
                String innerFlowableGp = Utils.getGenericParadigm(elementReturnType.toString(), "io.reactivex.Flowable<");
                if (annotationProcessor.mElements.getTypeElement(innerFlowableGp) != null) {
                    String superClassStr = annotationProcessor.mElements.getTypeElement(innerFlowableGp).getSuperclass().toString();
                    //是否继承BaseResponseList 或者 BaseResponse
                    if (superClassStr.contains(baseListRespPath)) { //Entry:Login2:BaseListResponse<Login2>     ApiService:Flowable<Login2>
                        methodSpec = createMethodByResponseList(executableElement, isIgnoreRrefreshToken, isIgnoreHandleError);
                    } else if (superClassStr.contains(baseRespPath)) { //ApiService:Flowable<BaseResponse<Any>>
                        methodSpec = createMethodByResponse(executableElement, isIgnoreRrefreshToken, isIgnoreHandleError); //Entry:Login2:BaseResponse<Login2>   ApiSerive:Flowable<Login2>
                    }
                } else if (innerFlowableGp.contains(baseListRespPath)) { //不是继承而是直接就是BaseResponseList
                    methodSpec = createMethodByResponseList(executableElement, isIgnoreRrefreshToken, isIgnoreHandleError);
                } else {
                    methodSpec = createMethodByResopnseObj(executableElement, isIgnoreRrefreshToken, isIgnoreHandleError);

                }
                if (methodSpec == null) {
                    annotationProcessor.mMessager.printMessage(Diagnostic.Kind.ERROR, "请检查ApiService中的对象是否继承EasyRespIface");
                    continue;
                }
                classbuilder.addMethod(methodSpec);
            }

            JavaFile javaFile = JavaFile.builder(apiRepoertoryPath, classbuilder.build())
                    .build();// 生成源代码
            try {
                javaFile.writeTo(annotationProcessor.mFiler);// 在 annotationProcessor.apiRepoertoryPath 生成一份源代码
            } catch (IOException e) {
            }
        }

    }


    /**
     * 生成继承ResponseList的方法
     *
     * @param executableElement
     * @return
     */
    private MethodSpec createMethodByResponseList(ExecutableElement executableElement, boolean isIgnoreRrefreshToken, boolean isIgnoreHandleError) {

        //Flowable<BaseListResponse<Login2>> -> BaseListResponse<Login2>
        String genericParadigm = Utils.getGenericParadigm(executableElement.getReturnType().toString(), "io.reactivex.Flowable<");
        //BaseListResponse<Login2> -> Login2
        String innerGp = Utils.getGenericParadigm(genericParadigm, baseListRespPath + "<");

        // =====   List<Login2>  ========
        ClassName list = ClassName.get("java.util", "List");
        TypeName listOfInnerGp = ParameterizedTypeName.get(list, annotationProcessor.getTypeName(innerGp));

        // =====  Flowable<List<Login2>>
        ClassName flowable = ClassName.get("io.reactivex", "Flowable");
        TypeName listOfFlowable = ParameterizedTypeName.get(flowable, listOfInnerGp);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                .addJavadoc("@ Api请求实现,由Apt自动生成,在此上添加代码无效")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(listOfFlowable);

        String autoCode = "return $T.getINSTANCE().getEasyService().$L($L)" +
                ".map($T.<$T>handleListResult())" +
                ".retryWhen($T.refreshToken().doRefresh())" +
                ".onErrorResumeNext($T.<$T>httpErrorHandle())" +
                ".compose($T.<$T>io_main())";

        return addStatement(builder, executableElement, isIgnoreRrefreshToken, isIgnoreHandleError, autoCode, annotationProcessor.getTypeName(innerGp),listOfInnerGp);
    }


    /**
     * 生成不实现Baseresponse<T> 中 T 的 方法
     *
     * @param executableElement
     * @return
     */
    private MethodSpec createMethodByResopnseObj(ExecutableElement executableElement, boolean isIgnoreRrefreshToken, boolean isIgnoreHandleError) {

        //====== BaseResponse<Object> ============
        String[] baseRespSplit = baseRespPath.split("\\.");
        String baseResp = baseRespSplit[baseRespSplit.length - 1];
        ClassName objClassName = ClassName.get(baseRespPath.replace("." + baseResp, ""), baseResp);
        TypeName baseResponse = ParameterizedTypeName.get(objClassName, annotationProcessor.getTypeName("java.lang.Object"));

        // Flowable<BaseResponse<Object>>
        ClassName flowable = ClassName.get("io.reactivex", "Flowable");
        TypeName listOfFlowable = ParameterizedTypeName.get(flowable, baseResponse);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                .addJavadoc("@ Api请求实现,由Apt自动生成,在此上添加代码无效")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(listOfFlowable);

        String autoCode = "return $T.getINSTANCE().getEasyService().$L($L)" +
                ".map($T.<$T>handleNoDataResult())." +
                "retryWhen($T.refreshToken().doRefresh())" +
                ".onErrorResumeNext($T.<$T>httpErrorHandle())" +
                ".compose($T.<$T>io_main())";

        return addStatement(builder, executableElement, isIgnoreRrefreshToken, isIgnoreHandleError, autoCode, baseResponse,null);
    }


    /**
     * 生成继承BaseResponse的方法
     *
     * @param executableElement
     * @return
     */
    private MethodSpec createMethodByResponse(ExecutableElement executableElement, boolean isIgnoreRrefreshToken, boolean isIgnoreHandleError) {
        // Flowable<Login> -> Login
        String genericParadigm = Utils.getGenericParadigm(executableElement.getReturnType().toString(), "io.reactivex.Flowable<");
        MethodSpec.Builder builder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                .addJavadoc("@ Api请求实现,由Apt自动生成,在此上添加代码无效")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.get(executableElement.getReturnType()));


        String autoCode = "return $T.getINSTANCE().getEasyService().$L($L)" +
                ".map($T.<$T>handleResult()).retryWhen($T.refreshToken().doRefresh())" +
                ".onErrorResumeNext($T.<$T>httpErrorHandle())" +
                ".compose($T.<$T>io_main())";

        return addStatement(builder, executableElement, isIgnoreRrefreshToken, isIgnoreHandleError, autoCode, annotationProcessor.getTypeName(genericParadigm),null);

    }

    /**
     * 组装代码
     *
     * @param builder
     * @param executableElement
     * @param autoCode
     * @param genericParadigm
     * @return
     */
    private MethodSpec addStatement(MethodSpec.Builder builder, ExecutableElement executableElement, boolean isIgnoreRefreshToken, boolean isIgnoreErrorHandle, String autoCode, TypeName genericParadigm,TypeName errorHandleGenericParadigm) {

        String parameterStr = getParameterStr(executableElement, builder);


        if (isIgnoreRefreshToken) {
            autoCode = autoCode.replace(".retryWhen($T.refreshToken().doRefresh())", "");
        }
        if (isIgnoreErrorHandle) {
            autoCode = autoCode.replace(".onErrorResumeNext($T.<$T>httpErrorHandle())", "");
        }
        if (isIgnoreRefreshToken && isIgnoreErrorHandle) {
            builder.addStatement(autoCode
                    , annotationProcessor.getTypeName(apiPath)
                    , executableElement.getSimpleName().toString()
                    , parameterStr
                    , annotationProcessor.getTypeName(easyApiPath)
                    , genericParadigm
                    , annotationProcessor.getTypeName(easyApiPath)
                    , errorHandleGenericParadigm == null?genericParadigm:errorHandleGenericParadigm
            );
        } else if (isIgnoreRefreshToken) {
            builder.addStatement(autoCode
                    , annotationProcessor.getTypeName(apiPath)
                    , executableElement.getSimpleName().toString()
                    , parameterStr
                    , annotationProcessor.getTypeName(easyApiPath)
                    , genericParadigm
                    , annotationProcessor.getTypeName(easyApiPath)
                    , errorHandleGenericParadigm == null?genericParadigm:errorHandleGenericParadigm
                    , annotationProcessor.getTypeName(easyApiPath)
                    , errorHandleGenericParadigm == null?genericParadigm:errorHandleGenericParadigm
            );
        } else if (isIgnoreErrorHandle) {

            builder.addStatement(autoCode
                    , annotationProcessor.getTypeName(apiPath)
                    , executableElement.getSimpleName().toString()
                    , parameterStr
                    , annotationProcessor.getTypeName(easyApiPath)
                    , genericParadigm
                    , annotationProcessor.getTypeName(easyApiPath)
                    , annotationProcessor.getTypeName(easyApiPath)
                    , errorHandleGenericParadigm == null?genericParadigm:errorHandleGenericParadigm
            );
        } else {
            builder.addStatement(autoCode
                    , annotationProcessor.getTypeName(apiPath)
                    , executableElement.getSimpleName().toString()
                    , parameterStr
                    , annotationProcessor.getTypeName(easyApiPath)
                    , genericParadigm
                    , annotationProcessor.getTypeName(easyApiPath)
                    , annotationProcessor.getTypeName(easyApiPath)
                    , errorHandleGenericParadigm == null?genericParadigm:errorHandleGenericParadigm
                    , annotationProcessor.getTypeName(easyApiPath)
                    , errorHandleGenericParadigm == null?genericParadigm:errorHandleGenericParadigm
            );
        }

        return builder.build();
    }

    /**
     * 获取方法中的传参
     *
     * @param executableElement
     * @param builder
     * @return
     */
    private String getParameterStr(ExecutableElement executableElement, MethodSpec.Builder builder) {

        if (paramsReplaceKey.length != paramsReplaceValue.length) {
            annotationProcessor.mMessager.printMessage(Diagnostic.Kind.ERROR, "请检查paramsReplaceKey 和 paramsReplaceValue 是否相等");
            return "";
        }

        String parameterStr = "";
        for (int i = 0; i < executableElement.getParameters().size(); i++) {
            if (i > 0) {
                parameterStr += ",";
            }
            VariableElement variableElement = executableElement.getParameters().get(i);
            //查看是否需要替换掉入参
            int containsIndex = contains(paramsReplaceKey, variableElement.getSimpleName().toString());
            if (containsIndex > -1) {
                parameterStr += paramsReplaceValue[containsIndex];
                continue;
            }
            parameterStr += variableElement.getSimpleName().toString();
            builder.addParameter(TypeName.get(variableElement.asType()), variableElement.getSimpleName().toString());
        }

        return parameterStr;
    }

    /**
     * 判断值是否相等
     *
     * @param strings
     * @param value
     * @return
     */
    private int contains(String[] strings, String value) {
        if (strings == null || strings.length == 0) return -1;
        for (int i = 0; i < strings.length; i++) {
            String key = strings[i];
            if (key.equals(value)) return i;
        }
        return -1;
    }

    /**
     * 是否含有 IgnoreRefreshToken 注解
     *
     * @param annotationMirrors
     * @return
     */
    private boolean isHadIgnoreRefreshToken(List<? extends AnnotationMirror> annotationMirrors) {
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            if (annotationMirror.toString().equals(C.IGNORE_REFRESH_TOKEN)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否含有 IgnoreErrorHandle 注解
     *
     * @param annotationMirrors
     * @return
     */
    private boolean isHadIgnoreErrorHandle(List<? extends AnnotationMirror> annotationMirrors) {
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            if (annotationMirror.toString().equals(C.IGNORE_ERROR_HANDLE)) {
                EasyErrorHandle errorHandle = annotationMirror.getAnnotationType().asElement().getAnnotation(EasyErrorHandle.class);
                return errorHandle.ignore();
            }
        }
        return false;
    }

}
