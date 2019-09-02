package com.wsg.annotation

import javax.lang.model.element.Element
import kotlin.reflect.KClass

/**
 * 存储路由的信息类
 */
open class RouterMeta constructor(
    var type: Type?,
    var element: Element?,
    var destinatio: KClass<*>?,
    var path: String,
    var group: String?
) {
    constructor(
        type: Type,
        element: Element,
        router: Router
    ) : this(type, element, null, router.path, router.group)

    constructor(path: String) : this(null,null,null,path,null)


    companion object {
        fun build(type: Type, destination: KClass<*>, path: String, group: String): RouterMeta {
            return RouterMeta(type, null, destination, path, group)
        }
    }

    /**
     * 路由的类型枚举
     */
    enum class Type {
        ACTIVITY, SERVICE, FRAGMENT
    }


}
