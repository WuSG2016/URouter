package com.wsg.annotation

/**
 * 类路径注解
 */

/**
 * @param group 设置路由组，若为空以路由地址的第一个来作为组名main
 * @param path /main/tess 设置路由地址
 */
@Target(AnnotationTarget.CLASS) // 作用在类上
@Retention(AnnotationRetention.BINARY)
annotation class Router(val path:String,val group:String="")