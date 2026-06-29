package top.yjp.my.app.db

import org.h2.jdbcx.JdbcDataSource
import java.io.File
import javax.sql.DataSource

object Database {
    private val dbDir: File by lazy {
        // Use system property if set, otherwise fallback to user home then temp
        val propPath = System.getProperty("todo.db.path")
        if (propPath != null) {
            File(propPath).also { it.mkdirs() }
        } else {
            // Try user home first, if not writable use project dir / temp
            val homeDir = File(System.getProperty("user.home"), ".todo_app")
            if (homeDir.isDirectory || homeDir.mkdirs()) {
                // Verify it's writable
                val testFile = File(homeDir, ".write_test")
                try {
                    if (testFile.createNewFile()) {
                        testFile.delete()
                    }
                    homeDir
                } catch (e: Exception) {
                    System.err.println("[WARN] Cannot write to $homeDir, using temp directory: ${e.message}")
                    File(System.getProperty("java.io.tmpdir"), "todo_app_db").also { it.mkdirs() }
                }
            } else {
                File(System.getProperty("java.io.tmpdir"), "todo_app_db").also { it.mkdirs() }
            }
        }
    }

    private val dataSource: JdbcDataSource by lazy {
        val url = "jdbc:h2:file:${dbDir.absolutePath}/todo_db;DB_CLOSE_DELAY=-1;MODE=MySQL"
        JdbcDataSource().apply {
            setURL(url)
            user = "sa"
            password = ""
        }
    }

    fun getDataSource(): DataSource = dataSource

    fun init() {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS tasks (
                        id VARCHAR(36) PRIMARY KEY,
                        title VARCHAR(500) NOT NULL,
                        description VARCHAR(2000) DEFAULT '',
                        status VARCHAR(20) NOT NULL DEFAULT 'TODO',
                        priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
                        due_date VARCHAR(10) NOT NULL DEFAULT '',
                        created_at VARCHAR(50) NOT NULL,
                        completed_at VARCHAR(50) DEFAULT NULL,
                        tags VARCHAR(500) DEFAULT '',
                        user_id VARCHAR(36) DEFAULT ''
                    )
                """.trimIndent())
                // Migrate existing columns if table already existed with smaller size
                try { stmt.execute("ALTER TABLE tasks ALTER COLUMN created_at VARCHAR(50)") } catch (_: Exception) { }
                try { stmt.execute("ALTER TABLE tasks ALTER COLUMN completed_at VARCHAR(50)") } catch (_: Exception) { }
                // Add user_id column for existing tables
                try { stmt.execute("ALTER TABLE tasks ADD COLUMN user_id VARCHAR(36) DEFAULT ''") } catch (_: Exception) { }

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS tags (
                        id VARCHAR(36) PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        icon VARCHAR(10) DEFAULT ''
                    )
                """.trimIndent())

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id VARCHAR(36) PRIMARY KEY,
                        username VARCHAR(100) NOT NULL UNIQUE,
                        password_hash VARCHAR(200) NOT NULL DEFAULT '',
                        nickname VARCHAR(100) DEFAULT '',
                        avatar VARCHAR(500) DEFAULT '',
                        token VARCHAR(100) DEFAULT '',
                        provider VARCHAR(20) DEFAULT 'local',
                        provider_id VARCHAR(200) DEFAULT '',
                        created_at VARCHAR(50) NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
