package com.wsg.core

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object DefaultPoolExecutor {
    private val mThreadFactory: ThreadFactory = object : ThreadFactory {
        private val atomicInteger = AtomicInteger(1)

        override fun newThread(r: Runnable): Thread {
            return Thread(r, "URouter #" + atomicInteger.getAndIncrement())
        }
    }


    //核心线程和最大线程都是cpu核心数+1
    private val CPU_COUNTS = Runtime.getRuntime().availableProcessors()
    private val MAX_CORE_POOL_SIZE = CPU_COUNTS + 1
    //存活30秒 回收线程
    private const val SURPLUS_THREAD_LIFE = 30L

    fun newDefaultPoolExecutor(corePoolSize: Int): ThreadPoolExecutor? {
        var corePoolSize = corePoolSize
        if (corePoolSize == 0) {
            return null
        }
        corePoolSize = Math.min(corePoolSize, MAX_CORE_POOL_SIZE)
        val threadPoolExecutor = ThreadPoolExecutor(
            corePoolSize,
            corePoolSize, SURPLUS_THREAD_LIFE, TimeUnit.SECONDS, ArrayBlockingQueue(64), mThreadFactory
        )
        //核心线程也会被销毁
        threadPoolExecutor.allowCoreThreadTimeOut(true)
        return threadPoolExecutor
    }
}