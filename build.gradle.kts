import groovy.util.Node
import groovy.util.NodeList

plugins {
    `java-library`
    `maven-publish`

    // lombok
    id("io.freefair.lombok") version "6.2.0"

    // shadowJar
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.mineclay"
version = "1.4.1-SNAPSHOT"

allprojects {
    plugins.apply("java-library")
    java {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }
    tasks.compileJava {
        options.encoding = "UTF-8"
    }
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
    mavenCentral()
}

dependencies {
    implementation(project(":nativeport:api"))
    implementation(project(":nativeport:v1_12_R2"))
    implementation(project(":nativeport:v1_18_R2"))

    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")

    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    api("org.jetbrains:annotations:22.0.0")

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

tasks.shadowJar {
    archiveClassifier.set("")
    dependencies {
        exclude(dependency("org.jetbrains:"))
    }
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                withXml {
                    val getGroupId: (Any) -> String = {
                        ((((it as Node).get("groupId") as NodeList).first() as Node).value() as NodeList).first() as String
                    }
                    val dependencies = (asNode().get("dependencies") as NodeList).first() as Node
                    dependencies.children().removeIf {
                        getGroupId(it) == "tclite.nativeport"
                    }
                }
            }
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
