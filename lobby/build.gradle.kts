plugins {
    id("java")
    alias(libs.plugins.paperweight)
}

group = "club.revived.lobby"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper)
    implementation(libs.jedis)
    implementation(project(":commons"))
}

tasks.test {
    useJUnitPlatform()
}