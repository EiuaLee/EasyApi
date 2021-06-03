package com.eiualee.demo.bean

import com.eiualee.easyapi.iface.EasyRespIface

open class BaseResp<T>: EasyRespIface<T> {
    var code: Int = 0
    var msg: String = ""
    var data: T? = null


    /**
     * 是否请求成功
     * @return Boolean
     */
    fun isStatusOk():Boolean{
        return code == 10200
    }

    /**
     * 自己定义的token过期
     * @return Boolean
     */
    fun isTokenExpire():Boolean{
        return code == 10401
    }

    override fun getRespData(): T? {
        return data
    }

    override fun getRespListData(): MutableList<T>? {
        return null
    }

}