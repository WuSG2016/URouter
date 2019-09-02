package com.wsg.module

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.wsg.annotation.Router

@Router(path = "/modle/app")
class Module_MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_activity_main)
        Log.e("dad","我是Module2")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("dad","销毁")
    }
}
