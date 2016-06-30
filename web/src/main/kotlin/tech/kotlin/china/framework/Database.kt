package tech.kotlin.china.framework

import org.apache.ibatis.annotations.*
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.log4j.Logger
import org.springframework.stereotype.Service
import utils.dataflow.next
import utils.decorator.ClassDecorator
import utils.decorator.decorateTo
import utils.properties.Env
import utils.properties.Props
import utils.properties.p
import java.lang.reflect.Method

@Service
class Database {

    var testMode = false

    val SESSION_FACTORY: SqlSessionFactory = Resources.getResourceAsStream("mybatis-config.xml").use {
        val properties = Props
                .p("jdbc.driver", Env["jdbc_driver"]) { "com.mysql.jdbc.Driver" }
                .p("jdbc.url", Env["jdbc_url"]) {
                    "jdbc:mysql://192.168.99.100:3306/kotlin_china?useUnicode=true&characterEncoding=UTF-8"
                }
                .p("jdbc.username", Env["jdbc_username"]) { "root" }
                .p("jdbc.password", Env["jdbc_password"]) { "root" }
        SqlSessionFactoryBuilder().build(it, properties)
    }

    fun <T> dbRead(action: (SqlSession) -> T): T = when (testMode) {
        false -> SESSION_FACTORY.openSession(true).use(action)

        true -> SESSION_FACTORY.openSession(false).use {
            try {
                action(it)
            } finally {
                it.rollback()
            }
        }
    }

    fun <T> dbWrite(action: (SqlSession) -> T): T = SESSION_FACTORY.openSession(false).use {
        when (testMode) {
            false -> try {
                val result = action.invoke(it)
                it.commit()
                return result;
            } catch (e: Throwable) {
                it.rollback()
                throw e
            }

            true -> try {
                action(it)
            } finally {
                it.rollback()
            }
        }
    }
}

inline fun <reified T : Any> SqlSession.of() = getMapper(T::class.java).decorateTo<T>(SQLLog(this))

class SQLLog(val session: SqlSession) : ClassDecorator {

    private val log = Logger.getLogger("SQLLog")

    private fun sqlLog(method: Method, args: Array<Any>): String {
        val select = method.getDeclaredAnnotation(Select::class.java)
        val delete = method.getDeclaredAnnotation(Delete::class.java)
        val update = method.getDeclaredAnnotation(Update::class.java)
        val insert = method.getDeclaredAnnotation(Insert::class.java)
        val selectProvider = method.getDeclaredAnnotation(SelectProvider::class.java)
        val sql = when {
            select != null -> select.value[0]
            delete != null -> delete.value[0]
            update != null -> update.value[0]
            insert != null -> insert.value[0]
            selectProvider != null -> selectProvider.type.java.newInstance()
            else -> "UNKNOWN SQL"
        }
        val params = StringBuilder() next {
            for (i in 0..args.size - 1)
                it.append(args[i]).append("(").append(method.parameterTypes[i].simpleName).append(")").append("\t")
        }
        return """
        |----------------------------------------------
        |Session: ${session.hashCode()}
        |SQL: $sql
        |Mapping method: ${method.name}
        |Params: ${if (params.length == 0) "<None>" else params.toString()}
        """.trimMargin("|")
    }

    override fun onResult(method: Method, result: Any?, args: Array<Any>): Any? {
        log.info("""
        |${sqlLog(method, args)}
        |Method success with result: $result
        """.trimMargin("|"))
        return result
    }

    override fun onError(method: Method, error: Throwable, args: Array<Any>): Throwable {
        log.warn("""
        |${sqlLog(method, args)}
        |Method fail with error
        """.trimMargin("|"))
        return error
    }
}