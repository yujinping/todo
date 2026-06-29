package top.yjp.my.app.repository

import top.yjp.my.app.model.Tag

interface ITagRepository {
    suspend fun getAllTags(): List<Tag>
    suspend fun getTagById(id: String): Tag?
    suspend fun createTag(tag: Tag): Tag
    suspend fun updateTag(tag: Tag): Tag
    suspend fun deleteTag(id: String)
}
