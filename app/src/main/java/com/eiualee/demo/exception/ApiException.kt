package com.xinswallow.lib_common.exception

import java.lang.Exception

class ApiException:Exception {

    var code:Int = 0
    var msg:String = ""

    constructor(cause: Throwable?, code: Int) : super(cause) {
        this.code = code
    }
}