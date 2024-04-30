plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.6.3"
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

repositories {
    // paper repo
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    mavenCentral()
}

dependencies {
    api(project(":nativeport:api"))
    paperweight.paperDevBundle("1.18.2-R0.1-SNAPSHOT")

    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
}
