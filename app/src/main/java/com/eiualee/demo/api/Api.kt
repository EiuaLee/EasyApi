package com.eiualee.demo.api

import android.net.ParseException
import com.blankj.utilcode.util.SPStaticUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.eiualee.easyapi.EasyApi
import com.eiualee.demo.bean.BaseListResp
import com.eiualee.demo.bean.BaseResp
import com.eiualee.demo.bean.RefreshTokenResp
import com.eiualee.demo.exception.LoginException
import com.eiualee.easyapi.iface.EasyApiServiceIface
import com.eiualee.easyapi.iface.EasyRefreshTokenIface
import com.eiualee.easyapi.iface.EasyRespIface
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.xinswallow.lib_common.exception.*
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * @作者 eiualee
 * @创建日期 2019/4/9 下午2:08
 * @描述  接口请求类
 */
class Api: EasyApiServiceIface<ApiService> {

    private var apiService: ApiService
    private val logInterceptor = HttpLoggingInterceptor()
    private val okHttpClient: OkHttpClient


    private val UNAUTHORIZED = 401
    private val FORBIDDEN = 403
    private val NOT_FOUND = 404
    private val REQUEST_TIMEOUT = 408
    private val INTERNAL_SERVER_ERROR = 500
    private val BAD_GATEWAY = 502
    private val SERVICE_UNAVAILABLE = 503
    private val GATEWAY_TIMEOUT = 504


    companion object {
        @JvmStatic
        val INSTANCE: Api by lazy(lock = LazyThreadSafetyMode.SYNCHRONIZED) { Api() }
        const val BASE_URL = "http://www.xxx.com/"
        const val RESET_LOGIN = 505

        //=========== 自定义 ===========
        //协议出错
        const val HTTP_ERROR = 1003
        //未知错误
        const val UNKNOWN = 1000
        //解析错误
        const val PARSE_ERROR = 1001
        //网络错误
        const val NETWORD_ERROR = 1002
    }


    private val headInterceptor = Interceptor() { chain: Interceptor.Chain ->
        val builder = chain.request().newBuilder()
        //加token之类的信息
        chain.proceed(builder.build())
    }


    private constructor() {

        logInterceptor.level = HttpLoggingInterceptor.Level.BODY


        val builder = OkHttpClient.Builder()
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(headInterceptor)
            .cache(Cache(File(Utils.getApp().cacheDir, "cache"), 1024 * 1024 * 10))

        builder.addInterceptor(logInterceptor)
        okHttpClient = builder.build()

        apiService = createApiService(SPUtils.getInstance().getString("base_url", BASE_URL))


        initEsayApi()

    }

    /**
     * 初始化回调参数
     */
    private fun initEsayApi() {


        EasyApi.setOnEasyApiCallback(object : EasyApi.OnEasyApiCallback {

            override fun bindRefreshToken(): EasyRefreshTokenIface<out EasyRespIface<*>> {
                return object : EasyRefreshTokenIface<RefreshTokenResp> {
                    override fun doRefresh(): Function<Flowable<Throwable>, Flowable<RefreshTokenResp>> {
                        return Function { t ->
                            t.flatMap(Function<Throwable, Flowable<RefreshTokenResp>> { t ->
                                if (t is TokenExporeException) {
                                    return@Function INSTANCE.apiService.refreshToken()
                                        .map<RefreshTokenResp>(
                                            EasyApi.handleRefreshTokenResult()
                                        )
                                        .doOnNext(Consumer<RefreshTokenResp> { t ->
                                            if (t == null) {
                                                return@Consumer
                                            }
                                            // 保存新的Token
                                        })
                                }
                                Flowable.error(t)
                            })
                        }
                    }
                }
            }

            override fun <T> originalDataHandle(t: T?): Exception? {
                when (t) {
                    is BaseResp<*> -> {
                        if (!t.isStatusOk()) return ServerException(t.msg, t.code)
                        if (t.isTokenExpire()) return TokenExporeException(t.msg, t.code)
                    }
                    is BaseListResp<*> -> {
                        if (!t.isStatusOk()) return ServerException(t.msg, t.code)
                        if (t.isTokenExpire()) return TokenExporeException(t.msg, t.code)
                    }
                }
                return null
            }

            override fun <T> refreshTokenOriginalDataHandle(t: T?): Exception? {
                //假如刷新Token之后还是报错，自定义一个异常，让程序回到登录页
                when (t) {
                    is BaseResp<*> -> {
                        if (!t.isStatusOk()) return LoginException(t.msg, t.code)
                        if (t.isTokenExpire()) return LoginException(t.msg, t.code)
                    }
                    is BaseListResp<*> -> {
                        if (!t.isStatusOk()) return LoginException(t.msg, t.code)
                        if (t.isTokenExpire()) return LoginException(t.msg, t.code)
                    }
                }
                return null
            }

            override fun exceptionHandle(e: Throwable): Exception {
                var apiException: ApiException

                if (e is HttpException) {  //HTTP错误
                    apiException = ApiException(e, HTTP_ERROR)
                    when (e.code()) {
                        UNAUTHORIZED, FORBIDDEN, NOT_FOUND, REQUEST_TIMEOUT, GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE -> apiException.msg =
                            "服务器数据异常"
                    }
                    return apiException
                } else if (e is ServerException) {  //服务器返回的错误
                    apiException = ApiException(e, e.code)
                    apiException.msg = e.message ?: "服务器数据异常"
                    return apiException
                } else if (e is JsonParseException || e is JSONException || e is ParseException) {
                    apiException = ApiException(e, PARSE_ERROR) //均视为解析错误
                    apiException.msg = "数据解析失败"
                    return apiException
                } else if (e is SocketTimeoutException) {//均视为连接服务器超时
                    apiException = ApiException(e, NETWORD_ERROR)
                    apiException.msg = "连接服务器超时"
                    return apiException
                } else if (e is ConnectException) {
                    apiException = ApiException(e, NETWORD_ERROR)
                    apiException.msg = "连接服务器失败"  //均视为网络错误
                    return apiException
                } else if (e is TokenExporeException) {
                    apiException = ApiException(e, e.code)
                    apiException.msg = e.message ?: "服务器数据异常"
                    return apiException
                } else if (e is LoginException) {
                    apiException = ApiException(e, RESET_LOGIN)
                    apiException.msg = "重新登录"
                    return apiException
                } else {
                    apiException = ApiException(e, UNKNOWN)
                    apiException.msg = "未知错误:${e.toString()}"          //未知错误
                    return apiException
                }
                return apiException
            }

        })
    }


    /**
     * 创建新的实例，一般用于多环境切换
     * @param baseUrl String
     * @return ApiService
     */
    private fun createApiService(baseUrl: String): ApiService {

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().serializeNulls().create()
                )
            ) //此处不用Gson转换器改用String传转换器
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(baseUrl)
            .build()

        return retrofit.create(ApiService::class.java)
    }

    /**
     * 改变baseUrl
     * @param baseUrl String
     */
    fun changeApiBaseUrl(baseUrl: String, blockUrl: String) {
        SPStaticUtils.put("base_url", baseUrl)
        this.apiService = createApiService(baseUrl)
    }


    override fun getEasyService(): ApiService {
        return apiService
    }


}