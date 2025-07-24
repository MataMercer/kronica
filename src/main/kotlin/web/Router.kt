package org.matamercer.web

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.UnauthorizedResponse
import io.javalin.http.sse.SseClient
import org.matamercer.authorizeCheck
import org.matamercer.getCurrentUserRole
import org.matamercer.security.UserRole
import org.matamercer.web.controllers.ArticleController
import org.matamercer.web.controllers.AuthController
import org.matamercer.web.controllers.CharacterController
import org.matamercer.web.controllers.Controller
import org.matamercer.web.controllers.FileController
import org.matamercer.web.controllers.NotificationController
import org.matamercer.web.controllers.OAuthController
import org.matamercer.web.controllers.RequiredRole
import org.matamercer.web.controllers.Route
import org.matamercer.web.controllers.SseRoute
import org.matamercer.web.controllers.TimelineController
import org.matamercer.web.controllers.UserController
import java.lang.reflect.InvocationTargetException

class Router(
    private val controllers: List<Any>,
    private val app: Javalin
) {

    fun setupRoutes(){
        addRouteAuthorization()
        controllers.forEach {
            addRoutes(it)
        }
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
                }catch (e: InvocationTargetException){
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