package com.wsg.compiler

object Constants {


    const val ACTIVITY = "android.app.Activity"

    const val FRAGMENT = "android.app.Fragment"

    const val V4FRAGMENT = "android.support.v4.app.Fragment"

    const val _Q_ = "/"

    fun createClassNameStr(classNameStr: String): String {
        return "RouterGroup${classNameStr.capitalize()}Impl"
    }

    const val PACKAGE_NAME = "com.wsg.router"
}