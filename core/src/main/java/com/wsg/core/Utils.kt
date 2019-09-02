package com.wsg.core

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dalvik.system.DexFile
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch

object Utils {

    const val PACKAGE_NAME = "com.wsg.router"
    const val CLASS_NAME="RouterGroup"
    /**
     * 获得程序所有的apk(instant run会产生很多split apk)
     *
     * @param context
     * @return
     */
    private fun getSourcePath(context: Context): List<String>? {
        try {
            val applicationInfo = context
                .packageManager
                .getApplicationInfo(context.packageName, 0)//flags Annotation retention policy.
            val sourceList = ArrayList<String>()
            sourceList.add(applicationInfo.sourceDir)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (null != applicationInfo.splitSourceDirs) {
                    sourceList.addAll(listOf(*applicationInfo.splitSourceDirs))
                }
            }
            return sourceList
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }

    }

    /**
     * 根据包名 找到包下的类
     *
     * @param application
     * @param pageName
     * @return
     */
    @Throws(InterruptedException::class)
    fun getFileNameByPackageName(application: Application, pageName: String): Set<String> {
        val classNames = HashSet<String>()
        val sourcePath = getSourcePath(application)//apk 的资源路径
        //使用同步计数器判断均处理完成
        val countDownLatch = CountDownLatch(sourcePath!!.size)
        val threadPoolExecutor = DefaultPoolExecutor.newDefaultPoolExecutor(sourcePath.size)
        for (path in sourcePath) {
            threadPoolExecutor!!.execute {
                var dexFile: DexFile? = null
                try {
                    //加载apk中的dex遍历 获得所有包名为pageName的类
                    dexFile = DexFile(path)
                    val entries = dexFile.entries()
                    while (entries.hasMoreElements()) {
                        val className = entries.nextElement()
                        if (className.startsWith(pageName)) {
                            classNames.add(className)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    if (null != dexFile) {
                        try {
                            dexFile.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                    //释放1个
                    countDownLatch.countDown()
                }
            }
        }
        //等待执行完成
        countDownLatch.await()
        return classNames
    }
}