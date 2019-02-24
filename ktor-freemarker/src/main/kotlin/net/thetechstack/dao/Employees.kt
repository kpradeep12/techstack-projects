package net.thetechstack.dao

import org.jetbrains.exposed.sql.Table

object Employees: Table(){
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 50)
    val email = varchar("email", 100)
    val city = varchar("city", 50)
}