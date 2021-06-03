package com.xinswallow.lib_common.exception

import java.lang.Exception

class TokenExporeException: Exception {
    constructor() : super()

    var code:Int = 0

    constructor(message: String?, code: Int) : super(message) {
        this.code = code
    }
}