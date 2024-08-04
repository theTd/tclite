plugins {
    `java-library`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    api("org.jetbrains:annotations:22.0.0")
    api("org.jooq:joor-java-8:0.9.15")
}
