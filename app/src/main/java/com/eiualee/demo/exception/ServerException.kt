package com.xinswallow.lib_common.exception

import java.lang.Exception

class ServerException:Exception {

    var code:Int = 0

    constructor(message: String?, code: Int) : super(message) {
        this.code = code
    }
}