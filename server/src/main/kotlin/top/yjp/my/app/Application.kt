package top.yjp.my.app

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import top.yjp.my.app.db.Database
import top.yjp.my.app.di.serverModule
import top.yjp.my.app.repository.ITaskRepository
import top.yjp.my.app.repository.ITagRepository
import top.yjp.my.app.routes.authRoutes
import top.yjp.my.app.routes.exportRoutes
import top.yjp.my.app.routes.tagRoutes
import top.yjp.my.app.routes.taskRoutes

fun main() {
    Database.init()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        modules(serverModule)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    install(CORS) {
        anyHost()
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowHeader(io.ktor.http.HttpHeaders.Authorization)
    }

    routing {
        val koin = org.koin.core.context.GlobalContext.get()
        val taskRepo = koin.get<ITaskRepository>()
        val tagRepo = koin.get<ITagRepository>()

        get("/") {
            call.respondText("Todo App Server is running!")
        }

        get("/health") {
            call.respondText("OK")
        }

        authRoutes()
        taskRoutes(taskRepo)
        tagRoutes(tagRepo)
        exportRoutes(taskRepo)
    }
}
