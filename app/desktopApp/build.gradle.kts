import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.core)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    }
}

compose.desktop {
    application {
        mainClass = "top.yjp.my.app.MainKt"

        jvmArgs += listOf(
            "-Djava.io.tmpdir=${project.layout.buildDirectory.dir("tmp").get().asFile.absolutePath}",
            "-Duser.home=${project.projectDir}"
        )

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TodoApp"
            packageVersion = "1.0.0"
            macOS {
                bundleID = "top.yjp.my.app"
                appStore = false
            }
        }
    }
}