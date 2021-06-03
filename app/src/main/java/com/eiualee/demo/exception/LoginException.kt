package com.eiualee.demo.exception

import java.lang.Exception

class LoginException: Exception {
    constructor() : super()

    var code:Int = 0

    constructor(message: String?, code: Int) : super(message) {
        this.code = code
    }
}