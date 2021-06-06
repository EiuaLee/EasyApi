package com.eiualee.demo

import com.blankj.utilcode.util.ToastUtils
import com.eiualee.demo.api.Api
import com.xinswallow.lib_common.exception.ApiException
import io.reactivex.subscribers.DisposableSubscriber

/**
  * @作者 liweihua
  * @创建日期 2021/6/6 11:24 上午
  * @描述 数据处理
  */
open abstract class EasySubscriver<T> : DisposableSubscriber<T>() {

    override fun onError(t: Throwable?) {
        //这里可以统一处理一下您的错误信息
        if(t is ApiException){
            if(t.code == Api.RESET_LOGIN){
                //跳转登录页
                return
            }
            ToastUtils.showLong(t.msg?:return)
        }
    }

    override fun onComplete() {
    }
}