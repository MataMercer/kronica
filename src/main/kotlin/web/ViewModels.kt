package org.matamercer.web

import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.matamercer.domain.models.User

data class UserViewModel(
   @JvmField val name: String,
)

data class PageViewModel(
   @JvmField val ctx: Context,
   @JvmField val title: String,
   @JvmField val description: String,
   @JvmField val flash: List<String> = emptyList(),
   ){

   fun getCurrentUser(): User?{
      val id = ctx.sessionAttribute<String>("current_user_id")
      val role = ctx.sessionAttribute<String>("current_user_role")
      val name = ctx.sessionAttribute<String>("current_user_name")
      if(id.isNullOrBlank() || role.isNullOrBlank() || name.isNullOrBlank()){
         return null
      }
      return User(id = id.toLong(), role = enumValueOf(role), name = name )
   }

}
class HelloPage {
   @JvmField var userName: String? = null
}