plugins {
    id("java")
}

group = "club.revived.commons"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.paper)
    implementation(libs.anvilgui)
}

tasks.test {
    useJUnitPlatform()
}