package top.yjp.my.app.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import top.yjp.my.app.model.Tag
import top.yjp.my.app.model.api.TagListResponse

class RemoteTagRepositoryImpl(
    private val baseUrl: String = "http://localhost:8080",
    private val tokenProvider: (() -> String)? = null
) : ITagRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                isLenient = true
            })
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        expectSuccess = false
    }

    override suspend fun getAllTags(): List<Tag> {
        val response: TagListResponse = client.get("$baseUrl/api/tags").body()
        return response.tags
    }

    override suspend fun getTagById(id: String): Tag? {
        return try {
            val response = client.get("$baseUrl/api/tag/$id")
            if (response.status.isSuccess()) response.body<Tag>() else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createTag(tag: Tag): Tag {
        val response = client.post("$baseUrl/api/tag") {
            setBody(tag)
        }
        return response.body()
    }

    override suspend fun updateTag(tag: Tag): Tag {
        val response = client.put("$baseUrl/api/tag/${tag.id}") {
            setBody(tag)
        }
        return response.body()
    }

    override suspend fun deleteTag(id: String) {
        client.delete("$baseUrl/api/tag/$id")
    }
}
