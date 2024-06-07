import java.io.FileFilter

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "tclite"

File("").resolve("nativeport").listFiles(FileFilter { it.isDirectory })?.forEach {
    include(":nativeport-${it.name}")
    project(":nativeport-${it.name}").projectDir = File("nativeport").resolve(it.name)
}
