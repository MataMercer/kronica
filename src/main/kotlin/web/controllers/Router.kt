package org.matamercer.web.controllers

import io.javalin.Javalin
import io.javalin.http.Context
import org.matamercer.security.UserRole
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class Router(
    private val articleController: ArticleController,
    private val timelineController: TimelineController,
    private val userController: UserController,
    private val authController: AuthController,
    private val characterController: CharacterController,
    private val app: Javalin

) {

    fun setupRoutes(){
        addRoutes(articleController)
        addRoutes(timelineController)
        addRoutes(userController)
        addRoutes(authController)
        addRoutes(characterController)
    }

    private fun addRoutes(obj: Any){

        val controllerAnnotation = obj::class.java.getAnnotation(Controller::class.java)
        val pathPrefix = controllerAnnotation?.path ?: ""

        val methods = obj::class.java.methods
        methods.filter {
            it.isAnnotationPresent(Route::class.java)
        }.forEach{ method ->
            val routeAnnotation = method.getAnnotation(Route::class.java)
            val roleAnnotation = method.getAnnotation(RequiredRole::class.java)
            val handler:(Context)->Unit = { ctx: Context ->
                try {
                    method.invoke(obj, ctx)
                }catch (e:InvocationTargetException){
                    print("Invocation Target Exception")

                }
            }
            if (roleAnnotation == null){
                app.addHttpHandler(
                    routeAnnotation.type,
                    pathPrefix + routeAnnotation.path,
                    handler
                )
            }else{
                app.addHttpHandler(
                    routeAnnotation.type,
                    pathPrefix + routeAnnotation.path,
                    handler,
                    roleAnnotation.role
                )
            }
        }
    }

}