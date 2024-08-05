plugins {
    `java-library`
    `maven-publish`

    // lombok
    id("io.freefair.lombok") version "6.2.0"

    // shadowJar
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val clayUsername: String by project
val clayPassword: String by project
val gprUser: String by project
val gprKey: String by project

allprojects {
    plugins.apply("java-library")
    plugins.apply("maven-publish")

    if (path != ":") {
        group = "com.mineclay.tclite${path.split(":").dropLast(1).joinToString(".")}"
    }

    java {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }
    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    publishing {
        publications {
            create<MavenPublication>("default") {
                from(components["java"])
            }
        }
        repositories {
            val snapshot = version.toString().endsWith("-SNAPSHOT")
            if (clayUsername.isNotEmpty()) {
                maven("https://maven.mineclay.com/repository/zhua-${if (snapshot) "snapshot" else "release"}/") {
                    credentials {
                        username = clayUsername
                        password = clayPassword
                    }
                }
            }

            if (gprUser.isNotEmpty()) {
                maven("https://maven.pkg.github.com/theTd/tclite") {
                    credentials {
                        username = gprUser
                        password = gprKey
                    }
                }
            }
        }
    }
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
    mavenCentral()
}

dependencies {
    api(project(":nativeport-api"))
    api("com.github.cryptomorin:XSeries:11.2.0.1")

    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")

    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    api("org.jetbrains:annotations:22.0.0")
    api("com.google.code.findbugs:jsr305:3.0.2")

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

tasks.jar {
    val nativeports = project.subprojects.filter { it.path.startsWith(":nativeport") }
    dependsOn(nativeports.map { it.tasks.build })

    nativeports.map {
        it.tasks.jar.get().outputs.files.singleFile.absolutePath.replace("-dev.jar", ".jar")
    }.forEach {
        from(zipTree(file(it)))
    }
}
