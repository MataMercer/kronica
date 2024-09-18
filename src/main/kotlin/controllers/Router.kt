package org.matamercer.controllers

import io.javalin.Javalin
import io.javalin.http.Context
import org.matamercer.security.UserRole
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class Router(
    private val articleController: ArticleController,
    private val app: Javalin
) {

    fun setupRoutes(){
        addRoutes(articleController)
    }

    private fun addRoutes(obj: Any){
        val methods = obj::class.java.methods
        methods.filter {
            it.isAnnotationPresent(Route::class.java)
        }.forEach{ method ->
            val routeAnnotation = method.getAnnotation(Route::class.java)
            val roleAnnotation = method.getAnnotation(RequiredRole::class.java)
            val handler:(Context)->Unit = { ctx: Context ->
                method.invoke(obj, ctx)
            }
            if (roleAnnotation == null){
                app.addHttpHandler(
                    routeAnnotation.type,
                    routeAnnotation.path,
                    handler
                )
            }else{
                app.addHttpHandler(
                    routeAnnotation.type,
                    routeAnnotation.path,
                    handler,
                    roleAnnotation.role
                )
            }


        }
    }
}