plugins {
    id("java")
}

group = "club.revived.queue"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jedis)
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