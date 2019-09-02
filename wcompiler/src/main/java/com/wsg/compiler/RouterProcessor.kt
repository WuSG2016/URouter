package com.wsg.compiler

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.wsg.annotation.Router
import javax.annotation.processing.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

import com.wsg.annotation.RouterMeta
import java.util.*
import javax.lang.model.SourceVersion

import javax.tools.Diagnostic
import kotlin.collections.HashMap

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.wsg.annotation.IRouteGroup

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
/**
 * Router注解处理器
 */
class RouterProcessor : AbstractProcessor() {


    /**
     * 分组 key 组名  value 对应组的路由信息
     */
    private var groupMap = HashMap<String, RouterMeta>()


    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val types = LinkedHashSet<String>()
        types.add(Router::class.java.canonicalName)
        return types
    }

    /**
     * 日志打印
     */
    private fun log(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8
    }

    override fun process(typeElement: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        if (!typeElement.isNullOrEmpty()) {
            val annotatedSet = roundEnvironment!!.getElementsAnnotatedWith(Router::class.java)
            if (annotatedSet.isNotEmpty()) {
                processRouter(annotatedSet)
            }
        }
        return true

    }

    /**
     * APT 生成类
     */
    private fun processRouter(annotatedSet: Set<Element>) {
        var routerMeta: RouterMeta?
        val activity = processingEnv.elementUtils.getTypeElement(Constants.ACTIVITY)
        val fragment = processingEnv.elementUtils.getTypeElement(Constants.FRAGMENT)
        val v4Fragment = processingEnv.elementUtils.getTypeElement(Constants.V4FRAGMENT)
        for (element in annotatedSet) {
            val routerAnnotation = element.getAnnotation(Router::class.java)
            routerMeta = when {
                processingEnv.typeUtils.isSubtype(
                    element.asType(),
                    activity.asType()
                ) -> RouterMeta(RouterMeta.Type.ACTIVITY, element, routerAnnotation)
                processingEnv.typeUtils.isSubtype(
                    element.asType(),
                    fragment.asType()
                ) -> RouterMeta(RouterMeta.Type.FRAGMENT, element, routerAnnotation)
                processingEnv.typeUtils.isSubtype(
                    element.asType(),
                    v4Fragment.asType()
                ) -> RouterMeta(RouterMeta.Type.FRAGMENT, element, routerAnnotation)
                else -> {
                    log("Just Support Activity Router!")
                    return
                }
            }
            checkRouterGroup(routerMeta)
        }
        generatedGroupTable()

    }


    /**
     * 生成实现类IRouteRoot
     */
    private fun generatedGroupTable() {
        var classStr: String? = null
        val onLoadFun = FunSpec.builder("onLoad")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("map", HashMap::class.parameterizedBy(String::class, RouterMeta::class))
        with(onLoadFun) {
            for ((key, value) in groupMap) {
                classStr = value.group
                val className: String = value.element!!.simpleName.toString()
                val packName: String = value.element.toString().split(".$className")[0]
                addStatement(
                    "map.put(%S,%T.build(%T.%L,%T::class,%S,%S))",
                    key,
                    ClassName("com.wsg.annotation", "RouterMeta"),
                    ClassName("com.wsg.annotation", "RouterMeta.Type"),
                    value.type!!,
                    ClassName(packName, className),
                    value.path,
                    value.group!!
                )
            }

        }
        val file = FileSpec.builder(Constants.PACKAGE_NAME, Constants.createClassNameStr(classStr!!))
            .addType(
                TypeSpec.classBuilder(Constants.createClassNameStr(classStr!!))
                    .addSuperinterface(IRouteGroup::class)
                    .addFunction(onLoadFun.build())
                    .build()
            )
            .build()
        file.writeTo(processingEnv.filer)
    }


    private fun checkRouterGroup(routerMeta: RouterMeta): Boolean {
        return if (checkValid(routerMeta)) {
            if (!groupMap.containsKey(routerMeta.path))
                groupMap[routerMeta.path] = routerMeta
            else
                log("the same path!")
            true
        } else {
            log("check path error!")
            false
        }

    }

    private fun checkValid(routerMeta: RouterMeta): Boolean {
        //是否以/开头
        if (routerMeta.path.startsWith("/")) {
            //再检测是否包含Group
            if (routerMeta.group.isNullOrEmpty()) {
                //截取Group
                val group = routerMeta.path.substring(1, routerMeta.path.indexOf(Constants._Q_, 1))
                if (group.isNullOrEmpty()) {
                    return false
                }
                routerMeta.group = group
            }
            return true
        }
        return false

    }
}

