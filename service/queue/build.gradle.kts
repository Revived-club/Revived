plugins {
    id("java")
    alias(libs.plugins.shadow)
}

group = "club.revived.queue"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jedis)
    implementation(libs.jetbrainsannotations)
    implementation(project(":commons"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "club.revived.queue.bootstrap.Main"
        )
    }
}