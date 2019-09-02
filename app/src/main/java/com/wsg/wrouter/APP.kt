package com.wsg.wrouter

import android.app.Application
import com.wsg.core.URouter

class APP :Application(){
    override fun onCreate() {
        super.onCreate()
        URouter.instance.initRouter(this)
        System.out.println(122)
    }
}