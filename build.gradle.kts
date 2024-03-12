plugins {
    java
    `maven-publish`
}

group = "com.mineclay"
version = "1.1.0-SNAPSHOT"

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

repositories {
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    mavenCentral()
}

dependencies {
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
    compileOnly("org.bukkit:bukkit:1.12.2-R0.1-SNAPSHOT")

    compileOnly("org.jetbrains:annotations:22.0.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.bukkit:bukkit:1.12.2-R0.1-SNAPSHOT")
    testCompileOnly("org.jetbrains:annotations:22.0.0")
    // mockito
    testImplementation("org.mockito:mockito-inline:3.12.4")
    testImplementation("org.mockito:mockito-junit-jupiter:3.12.4")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri(
                "https://maven.mineclay.com/repository/zhua-${
                    if (version.toString().endsWith("SNAPSHOT")) "snapshot" else "release"
                }/"
            )
            credentials {
                username = findProperty("clayUsername").toString()
                password = findProperty("clayPassword").toString()
            }
        }
    }
}
