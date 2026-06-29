package top.yjp.my.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import top.yjp.my.app.db.Database
import top.yjp.my.app.db.TagRepositoryImpl
import top.yjp.my.app.db.TaskRepositoryImpl
import top.yjp.my.app.db.UserRepository
import top.yjp.my.app.repository.ITagRepository
import top.yjp.my.app.repository.ITaskRepository

val serverModule: Module = module {
    single { Database.getDataSource() }
    single<ITaskRepository> { TaskRepositoryImpl() }
    single<ITagRepository> { TagRepositoryImpl() }
}
