package org.matamercer.web.controllers

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.UnauthorizedResponse
import io.javalin.http.sse.SseClient
import io.javalin.websocket.WsConfig
import io.javalin.websocket.WsHandlerType
import org.matamercer.authorizeCheck
import org.matamercer.domain.models.Trait
import org.matamercer.getCurrentUser
import org.matamercer.getCurrentUserRole
import org.matamercer.security.UserRole
import java.lang.reflect.InvocationTargetException

class Router(
    private val articleController: ArticleController,
    private val timelineController: TimelineController,
    private val userController: UserController,
    private val authController: AuthController,
    private val oAuthController: OAuthController,
    private val characterController: CharacterController,
    private val fileController: FileController,
    private val notificationController: NotificationController,
    private val app: Javalin

) {

    fun setupRoutes(){
        addRouteAuthorization()
        addRoutes(articleController)
        addRoutes(timelineController)
        addRoutes(userController)
        addRoutes(authController)
        addRoutes(characterController)
        addRoutes(fileController)
        addRoutes(notificationController)
        addRoutes(oAuthController)
    }

    private fun addRouteAuthorization(){
        app.beforeMatched { ctx ->
            val routeRoles = ctx.routeRoles()

            if (routeRoles.isEmpty()) {
                return@beforeMatched
            }

            val method = ctx.req().method
            if ((method == "post" || method == "put" || method == "delete") &&
                ctx.req().getHeader("X-XSRF-TOKEN") != ctx.sessionAttribute<String>("csrf_token")
            ) {
                throw UnauthorizedResponse("Action is denied")
            }

            val userRole = getCurrentUserRole(ctx)
            if (routeRoles.first() == UserRole.UNAUTHENTICATED_USER && userRole != UserRole.UNAUTHENTICATED_USER) {
                throw UnauthorizedResponse("Access is denied")
            }

            if (authorizeCheck(userRole, routeRoles)) {
                return@beforeMatched
            } else if (userRole == UserRole.UNAUTHENTICATED_USER) {
                throw UnauthorizedResponse("Access is denied")
            } else {
                throw UnauthorizedResponse("Access is denied")
            }
        }
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
                //hide invocation target exception
                try {
                    method.invoke(obj, ctx)
                }catch (e:InvocationTargetException){
                    throw e.cause as Throwable
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

        methods.filter {
            it.isAnnotationPresent(SseRoute::class.java)
        }.forEach{ method ->
            val routeAnnotation = method.getAnnotation(SseRoute::class.java)
            val roleAnnotation = method.getAnnotation(RequiredRole::class.java)
            val handler:(SseClient)->Unit = { client: SseClient ->
                method.invoke(obj, client)

            }


            if (roleAnnotation == null){
                app.sse(
                    pathPrefix + routeAnnotation.path,
                    handler
                )
            }else{
                app.sse(
                    pathPrefix + routeAnnotation.path,
                    handler,
                    roleAnnotation.role
                )
            }
        }
    }

}