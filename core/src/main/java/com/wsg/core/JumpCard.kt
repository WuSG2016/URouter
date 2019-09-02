package com.wsg.core

import android.os.Bundle
import com.wsg.annotation.RouterMeta


class JumpCard(
    path: String,
    var bundle: Bundle? = null,
    var optionsCompat: Bundle? = null,
    var exitAnim: Int = 0,
    var enterAnim: Int = 0
) : RouterMeta(path) {

    fun navigation(): Any? {
        return URouter.instance.navigation(null, this, -1)
    }
}


