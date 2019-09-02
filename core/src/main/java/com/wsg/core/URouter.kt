package com.wsg.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.wsg.annotation.IRouteGroup
import com.wsg.annotation.RouterMeta
import java.lang.RuntimeException
import javax.security.auth.login.LoginException
import kotlin.reflect.KFunction

open class URouter private constructor() {
    var mHandler: Handler? = null
    var application: Application? = null
    private val classNameMap: Map<String, RouterMeta> = HashMap()

    fun initRouter(application: Application) {
        this.application = application
        loadRouteTable { it ->
            it.filter { it.startsWith(Utils.PACKAGE_NAME) }
        }
    }

    private fun loadRouteTable(t: (Set<String>) -> List<String>) {
        val setClassName = Utils.getFileNameByPackageName(application!!, Utils.PACKAGE_NAME)
        val classNameList: List<String>? = t(setClassName)
        if (classNameList!!.isNotEmpty()) {
            for (classNameStr in classNameList) {
                val routeGroup: Class<*> = Class.forName(classNameStr)
                val kFunction = routeGroup.getMethod("onLoad",HashMap::class.java)
                kFunction.invoke(routeGroup.newInstance(),classNameMap)
            }
        }
    }

    fun jump(path: String): JumpCard {
        if (path.isEmpty()) {
            throw RuntimeException("路径无效")
        }
        return JumpCard(path)
    }

    fun navigation(context: Context?, jumpCard: JumpCard, requestCode: Int): Any? {
        produceJumpCard({ classNameMap[it] }, jumpCard)
        val mContext = context ?: application
        when (jumpCard.type) {
            RouterMeta.Type.ACTIVITY -> {
                val intent = Intent(mContext, jumpCard.destinatio!!.java)
                if (jumpCard.bundle != null) {
                    intent.putExtras(jumpCard.bundle)
                }
                if (mContext !is Activity) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                //主线执行
                mHandler!!.post {
                    if (requestCode > 0) {
                        ActivityCompat.startActivityForResult(
                            mContext as Activity,
                            intent,
                            requestCode,
                            jumpCard.optionsCompat
                        )
                    } else {
                        ActivityCompat.startActivity(mContext!!, intent, jumpCard.optionsCompat)
                    }
                    if ((0 != jumpCard.enterAnim || 0 != jumpCard.exitAnim) && mContext is Activity) {
                        mContext.overridePendingTransition(jumpCard.enterAnim, jumpCard.exitAnim)
                    }
                }
            }
            RouterMeta.Type.FRAGMENT -> {

            }
            RouterMeta.Type.SERVICE -> {

            }
            else -> {
                Log.e("navigation", "没找到对应类型")
            }

        }
        return null

    }

    private fun produceJumpCard(findRouterMeta: (String) -> RouterMeta?, jumpCard: JumpCard) {
        val routerMeta = findRouterMeta(jumpCard.path)
        if (routerMeta != null) {
            jumpCard.destinatio = routerMeta.destinatio
            jumpCard.type = routerMeta.type
        } else {
            Log.e("produceJumpCard", "未找到匹配的RouterMeta")
        }
    }

    companion object {
        val instance: URouter by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            URouter()
        }
    }

    init {
        mHandler = Handler(Looper.getMainLooper())
    }
}


