package com.eiualee.demo.api

import com.easy_api.annotation.ApiFactory
import com.easy_api.annotation.EasyErrorHandle
import com.easy_api.annotation.EasyRefreshToken
import com.eiualee.demo.bean.BaseResp
import com.eiualee.demo.bean.RefreshTokenResp
import io.reactivex.Flowable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

@ApiFactory(
    createApiFilePath = "com.eiualee.demo.api",
//    createApiFileName = "ApiMine",
//    isEnableAutoRefreshToken = false,
    isEnableAutoHandleError = false,
    api = "com.eiualee.demo.api.Api",
    baseResp = "com.eiualee.demo.bean.BaseResp",
    baseListResp = "com.eiualee.demo.bean.BaseListResp"
)
interface ApiService {

    /**
     * 刷新token
     * @param refreshToken String
     * @return Flowable<LoginResponse>
     */
//    @EasyRefreshToken
    @EasyErrorHandle(ignore = false)
    @FormUrlEncoded
    @POST("common/refresh_token")
    fun refreshToken(): Flowable<RefreshTokenResp>


    /**
     * 登录接口
     * @return Flowable<LoginResponse>
     */
    @EasyRefreshToken
    @FormUrlEncoded
    @POST("app/login")
    fun login(@Field("mobile") mobile: String, @Field("code") code: String): Flowable<BaseResp<Any>>

}