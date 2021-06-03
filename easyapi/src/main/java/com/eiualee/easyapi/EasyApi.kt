package com.eiualee.easyapi

import com.eiualee.easyapi.iface.EasyApiServiceIface
import com.eiualee.easyapi.iface.EasyRefreshTokenIface
import com.eiualee.easyapi.iface.EasyRespIface
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * @作者 liweihua
 * @创建日期 2021/6/2 9:55 上午
 * @描述 EsayApi
 */
object EasyApi {

    private var onEasyApiCallback: OnEasyApiCallback? = null

    /**
     * 设置回调参数
     * @param onEasyApiCallback OnEasyApiCallback
     */
    @JvmStatic
    fun setOnEasyApiCallback(onEasyApiCallback: OnEasyApiCallback) {
        this.onEasyApiCallback = onEasyApiCallback
    }

    /**
     * 线程转换
     * @return FlowableTransformer<T, T>
     */
    @JvmStatic
    fun <T> io_main(): FlowableTransformer<T, T> {
        return FlowableTransformer<T, T> { upstream ->
            upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 处理返回的数据 要求转换的是一个实体
     * @return Function<EasyRespIface<T>, T>
     */
    @JvmStatic
    fun <T> handleResult(): Function<EasyRespIface<T>, T?> {
        if (onEasyApiCallback == null) {
            throw NullPointerException("OnEasyApiCallback 不能为空")
        }
        return Function { t ->
            val exception = onEasyApiCallback!!.originalDataHandle(t)
            if (exception != null) {
                throw exception
            }
            t.respData
        }
    }

    /**
     * 处理返回的数据 要求转换的是一个List
     * @return Function<EasyRespIface<T>, MutableList<T>>
     */
    fun <T> handleListResult(): Function<EasyRespIface<T>, MutableList<T>?> {
        if (onEasyApiCallback == null) {
            throw NullPointerException("OnEasyApiCallback 不能为空")
        }
        return Function { t ->
            val exception = onEasyApiCallback!!.originalDataHandle(t)
            if (exception != null) {
                throw exception
            }
            t.respListData
        }
    }

    /**
     * 处理返回数据，不要求转换
     * @return Function<EasyRespIface<T>, EasyRespIface<T>?>
     */
    @JvmStatic
    fun <T:EasyRespIface<*>> handleNoDataResult(): Function<T, T?> {
        if (onEasyApiCallback == null) {
            throw NullPointerException("OnEasyApiCallback 不能为空")
        }
        return Function { t ->
            val exception = onEasyApiCallback!!.originalDataHandle(t)
            if (exception != null) {
                throw exception
            }
            t
        }
    }


    /**
     * 处理请求结果
     * @return Function<BaseResponse<T>,T>
     */
    fun <T> handleRefreshTokenResult(): Function<EasyRespIface<T>, T?> {
        if (onEasyApiCallback == null) {
            throw NullPointerException("OnEasyApiCallback 不能为空")
        }
        return Function { t ->
            val exception = onEasyApiCallback!!.refreshTokenOriginalDataHandle(t)
            if (exception != null) {
                throw exception
            }
            t.respData
        }
    }


    /**
     * 刷新Token需要使用到的方法
     * @return EasyRefreshTokenIface<out EasyRespIface<*>>
     */
    @JvmStatic
    fun refreshToken(): EasyRefreshTokenIface<out EasyRespIface<*>> {
        if (onEasyApiCallback == null) {
            throw NullPointerException("OnEasyApiCallback 不能为空")
        }
        return onEasyApiCallback!!.bindRefreshToken()
    }


    /**
     * 处理返回的错误结果
     * @param T
     */
    @JvmStatic
    fun <T> httpErrorHandle(): Function<Throwable, Flowable<T>> {
        if (onEasyApiCallback == null) {
            throw NullPointerException("OnEasyApiCallback 不能为空")
        }
        return Function { t ->
            Flowable.error(onEasyApiCallback!!.exceptionHandle(t))
        }
    }



    /**
     * 处理所需要的回调
     */
    interface OnEasyApiCallback {
        /**
         * 绑定刷新Token的请求接口
         * @return EasyRefreshTokenIface<out EasyRespIface<*>>
         */
        fun bindRefreshToken(): EasyRefreshTokenIface<out EasyRespIface<*>>

        /**
         * 处理原始数据
         * @param t T
         * @return Exception
         */
        fun <T> originalDataHandle(t: T?): Exception?

        /**
         * 处理刷新token的原始数据
         * @param t T
         * @return Exception?
         */
        fun <T> refreshTokenOriginalDataHandle(t: T?): Exception?

        /**
         * 处理异常情况
         * @param e Throwable
         * @return Exception
         */
        fun exceptionHandle(e:Throwable):Exception
    }
}

