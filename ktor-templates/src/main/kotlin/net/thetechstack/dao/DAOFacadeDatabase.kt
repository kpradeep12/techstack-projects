package net.thetechstack.dao

import net.thetechstack.model.Employee
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.Closeable

interface DAOFacade: Closeable{
    fun init()
    fun createEmployee(name:String, email:String, city:String)
    fun updateEmployee(id:Int, name:String, email:String, city:String)
    fun deleteEmployee(id:Int)
    fun getEmployee(id:Int): Employee?
    fun getAllEmployees(): List<Employee>
}

class DAOFacadeDatabase(val db: Database): DAOFacade{

    override fun init() =
        transaction(db) {
            SchemaUtils.create(Employees)
            val employees = listOf(Employee(1, "Owlette","owlette@techstack.net", "New York"),
                    Employee(2, "Catboy","catboy@techstack.net", "New York"),
                    Employee(3, "Grekko","grekko@techstack.net", "New York"))
            Employees.batchInsert(employees){ employee ->
                this[Employees.id] = employee.id
                this[Employees.name] = employee.name
                this[Employees.email] = employee.email
                this[Employees.city] = employee.city
            }
            Unit
        }

    override fun createEmployee(name: String, email: String, city: String) =
        transaction(db) {
            Employees.insert {it[Employees.name] = name;
                it[Employees.email] = email; it[Employees.city] = city;
            }
            Unit
        }

    override fun updateEmployee(id: Int, name: String, email: String, city: String) =
        transaction(db) {
            Employees.update({Employees.id eq id}){
                it[Employees.name] = name
                it[Employees.email] = email
                it[Employees.city] = city
            }
            Unit
        }

    override fun deleteEmployee(id: Int) = transaction(db) {
        Employees.deleteWhere { Employees.id eq id }
        Unit
    }


    override fun getEmployee(id: Int) =
        transaction(db) {
            Employees.select { Employees.id eq id }.map {
                Employee(it[Employees.id], it[Employees.name], it[Employees.email], it[Employees.city]
                )
            }.singleOrNull()
        }

    override fun getAllEmployees() = transaction(db) {
        Employees.selectAll().map {
            Employee(it[Employees.id], it[Employees.name], it[Employees.email], it[Employees.city]
            )
        }
    }

    override fun close() { }
}