package top.yjp.my.app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import top.yjp.my.app.di.appModule

fun main() {
    startKoin {
        modules(appModule)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Todo 任务管理",
            state = WindowState(width = 480.dp, height = 800.dp),
        ) {
            App()
        }
    }
}
