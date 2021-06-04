package com.eiualee.demo.api

import com.easy_api.annotation.ApiFactory
import com.easy_api.annotation.EasyErrorHandle
import com.easy_api.annotation.EasyRefreshToken
import com.eiualee.demo.bean.BaseResp
import com.eiualee.demo.bean.RefreshTokenResp
import io.reactivex.Flowable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

@ApiFactory(
    createApiFilePath = "com.eiualee.demo.api",
//    createApiFileName = "ApiMine",
//    isEnableAutoRefreshToken = false,
    isEnableAutoHandleError = false,
    api = "com.eiualee.demo.api.Api",
    baseResp = "com.eiualee.demo.bean.BaseResp",
    baseListResp = "com.eiualee.demo.bean.BaseListResp",
    //细粒度的调整,请务必填入 完整包名带方法
    paramsReplaceKey = ["token","mobile"],
    paramsReplaceValue = ["com.eiualee.demo.utils.Token.getToken()","\"13800138000\""]
)
interface ApiService {

    /**
     * 刷新token
     * @param refreshToken String
     * @return Flowable<RefreshTokenResp>
     */
    @EasyRefreshToken(ignore = false)
    @FormUrlEncoded
    @POST("easyapi/refresh_token")
    fun refreshToken(): Flowable<RefreshTokenResp>


    /**
     * 演示停止不处理错误信息 @EasyErrorHandle
     * @param refreshToken String
     * @return Flowable<BaseResp<Any>>
     */
    @EasyErrorHandle
    @FormUrlEncoded
    @POST("easyapi/fun1")
    fun fun1(): Flowable<BaseResp<Any>>


    /**
     * 演示停止自动刷新token @EasyRefreshToken
     * @return Flowable<BaseResp<Any>>
     */
    @EasyRefreshToken
    @FormUrlEncoded
    @POST("easyapi/fun2")
    fun fun2(@Field("mobile") mobile: String, @Field("code") code: String): Flowable<BaseResp<Any>>


    /**
     * 演示细粒度的入参替换 paramsReplaceKey 替换token 这个入参
     * @return Flowable<BaseResp<Any>>
     */
    @FormUrlEncoded
    @POST("easyapi/fun3")
    fun fun3(@Header("easy-token") token: String): Flowable<BaseResp<Any>>

}