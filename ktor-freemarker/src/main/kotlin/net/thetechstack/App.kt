package net.thetechstack

import freemarker.cache.ClassTemplateLoader
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.Parameters
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import net.thetechstack.dao.DAOFacadeDatabase
import net.thetechstack.model.Employee
import org.jetbrains.exposed.sql.Database

val dao = DAOFacadeDatabase(Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver"))

fun main() {
    embeddedServer(Netty, 8080){
        dao.init()
        install(FreeMarker){
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        }
        routing {
            route("/"){
                get{
                    call.respond(FreeMarkerContent("index.ftl", mapOf("employees" to dao.getAllEmployees())))
                }
            }
            route("/employee"){
                get {
                    val action = (call.request.queryParameters["action"] ?: "new")
                    when(action){
                        "new" -> call.respond(FreeMarkerContent("employee.ftl",
                                    mapOf("action" to action)))
                        "edit" -> {
                            val id = call.request.queryParameters["id"]
                            if(id != null){
                                call.respond(FreeMarkerContent("employee.ftl",
                                        mapOf("employee" to dao.getEmployee(id.toInt()),
                                                "action" to action)))
                            }
                        }
                    }
                }
                post{
                    val postParameters: Parameters = call.receiveParameters()
                    val action = postParameters["action"] ?: "new"
                    when(action){
                        "new" -> dao.createEmployee(postParameters["name"] ?: "", postParameters["email"] ?: "", postParameters["city"] ?: "")
                        "edit" ->{
                            val id = postParameters["id"]
                            if(id != null)
                                dao.updateEmployee(id.toInt(), postParameters["name"] ?: "", postParameters["email"] ?: "", postParameters["city"] ?: "")
                        }
                    }
                    call.respond(FreeMarkerContent("index.ftl", mapOf("employees" to dao.getAllEmployees())))
                }
            }
            route("/delete"){
                get{
                    val id = call.request.queryParameters["id"]
                    if(id != null){
                        dao.deleteEmployee(id.toInt())
                        call.respond(FreeMarkerContent("index.ftl", mapOf("employees" to dao.getAllEmployees())))
                    }
                }
            }
        }
    }.start(wait = true)
}
