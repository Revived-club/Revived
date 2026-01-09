plugins {
    id("java")
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight)
}

group = "club.revived.duels"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://repo.codemc.io/repository/maven-releases") // PacketEvents
}

dependencies {
    paperweight.paperDevBundle(libs.versions.minecraft)
    implementation(libs.jedis)
    implementation(libs.mongo)
    compileOnly(libs.commandapi)
    implementation(project(":commons"))
    compileOnly(libs.anvilgui)
}

tasks.test {
    useJUnitPlatform()
}