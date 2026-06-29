package top.yjp.my.app.db

import top.yjp.my.app.model.Tag
import top.yjp.my.app.repository.ITagRepository
import java.sql.ResultSet
import java.util.*

class TagRepositoryImpl : ITagRepository {

    private val ds = Database.getDataSource()

    private fun ResultSet.toTag(): Tag = Tag(
        id = getString("id"),
        name = getString("name"),
        icon = getString("icon") ?: "🏷️"
    )

    override suspend fun getAllTags(): List<Tag> {
        val sql = "SELECT * FROM tags ORDER BY name"
        ds.connection.use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(sql)
                val tags = mutableListOf<Tag>()
                while (rs.next()) tags.add(rs.toTag())
                return tags
            }
        }
    }

    override suspend fun getTagById(id: String): Tag? {
        val sql = "SELECT * FROM tags WHERE id = ?"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                return if (rs.next()) rs.toTag() else null
            }
        }
    }

    override suspend fun createTag(tag: Tag): Tag {
        val id = if (tag.id.isBlank()) UUID.randomUUID().toString() else tag.id
        val sql = "INSERT INTO tags (id, name, icon) VALUES (?, ?, ?)"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, id)
                stmt.setString(2, tag.name)
                stmt.setString(3, tag.icon)
                stmt.executeUpdate()
            }
        }
        return tag.copy(id = id)
    }

    override suspend fun updateTag(tag: Tag): Tag {
        val sql = "UPDATE tags SET name=?, icon=? WHERE id=?"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, tag.name)
                stmt.setString(2, tag.icon)
                stmt.setString(3, tag.id)
                stmt.executeUpdate()
            }
        }
        return tag
    }

    override suspend fun deleteTag(id: String) {
        val sql = "DELETE FROM tags WHERE id = ?"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, id)
                stmt.executeUpdate()
            }
        }
    }
}
