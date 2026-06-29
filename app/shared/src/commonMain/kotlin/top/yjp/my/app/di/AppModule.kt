package top.yjp.my.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import top.yjp.my.app.repository.IAuthRepository
import top.yjp.my.app.repository.ITagRepository
import top.yjp.my.app.repository.ITaskRepository
import top.yjp.my.app.repository.RemoteAuthRepositoryImpl
import top.yjp.my.app.repository.RemoteTagRepositoryImpl
import top.yjp.my.app.repository.RemoteTaskRepositoryImpl

val appModule: Module = module {
    single<ITaskRepository> { RemoteTaskRepositoryImpl() }
    single<ITagRepository> { RemoteTagRepositoryImpl() }
    single<IAuthRepository> { RemoteAuthRepositoryImpl() }
}
