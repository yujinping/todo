package top.yjp.my.app.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import top.yjp.my.app.repository.ITagRepository
import top.yjp.my.app.model.Tag
import top.yjp.my.app.model.api.ApiResponse
import top.yjp.my.app.model.api.TagListResponse

private val tagJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

fun Route.tagRoutes(repository: ITagRepository) {
    get("/api/tags") {
        val tags = repository.getAllTags()
        call.respond(TagListResponse(tags))
    }

    post("/api/tag") {
        val tag: Tag
        try {
            val bodyText = call.receiveText()
            tag = tagJson.decodeFromString(bodyText)
        } catch (e: Exception) {
            return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse(false, "Invalid request body: ${e.message}")
            )
        }
        val created = repository.createTag(tag)
        call.respond(HttpStatusCode.Created, created)
    }

    put("/api/tag/{id}") {
        val id = call.parameters["id"] ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse(false, "Missing tag ID")
        )
        val existing = repository.getTagById(id)
        if (existing == null) {
            return@put call.respond(
                HttpStatusCode.NotFound,
                ApiResponse(false, "Tag not found")
            )
        }
        val tag: Tag
        try {
            tag = tagJson.decodeFromString(call.receiveText())
        } catch (e: Exception) {
            return@put call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse(false, "Invalid request body: ${e.message}")
            )
        }
        val updated = repository.updateTag(tag.copy(id = id))
        call.respond(updated)
    }

    delete("/api/tag/{id}") {
        val id = call.parameters["id"] ?: return@delete call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse(false, "Missing tag ID")
        )
        repository.deleteTag(id)
        call.respond(ApiResponse(true, "Tag deleted"))
    }
}
