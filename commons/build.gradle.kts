plugins {
    id("java")
}

group = "club.revived.commons"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.worldguard)
    implementation(libs.anvilgui)
    implementation(libs.kubernetes)
    compileOnly(libs.authlib)
}

tasks.test {
    useJUnitPlatform()
}