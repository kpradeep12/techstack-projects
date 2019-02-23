package net.thetechstack

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import net.thetechstack.dao.DAOFacadeDatabase
import net.thetechstack.model.Employee
import org.jetbrains.exposed.sql.Database

val dao = DAOFacadeDatabase(Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver"))
fun main() {
    embeddedServer(Netty, port = 8080){
        dao.init()
        install(CallLogging)
        install(ContentNegotiation){
            jackson {}
        }
        routing {
            route("/employees"){
                get {
                    call.respond(dao.getAllEmployees())
                }
                post {
                    val emp = call.receive<Employee>()
                    dao.createEmployee(emp.name, emp.email, emp.city)
                }
                put {
                    val emp = call.receive<Employee>()
                    dao.updateEmployee(emp.id, emp.name, emp.email, emp.city)
                }
                delete("/{id}") {
                    val id = call.parameters["id"]
                    if(id != null)
                        dao.deleteEmployee(id.toInt())
                }
            }
        }
    }.start(wait = true)
}

