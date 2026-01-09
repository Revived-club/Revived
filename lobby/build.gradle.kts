plugins {
    id("java")
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight)
}

group = "club.revived.lobby"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.pvphub.me/tofaa")
    maven("https://repo.codemc.io/repository/maven-releases")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperweight.paperDevBundle(libs.versions.minecraft)
    implementation(libs.jedis)
    implementation(libs.mongo)
    compileOnly(libs.commandapi)
    implementation(project(":commons"))
    implementation(libs.entitylib)
    compileOnly(libs.packetevents)
    compileOnly(libs.anvilgui)
    compileOnly(libs.worldguard)
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    relocate("me.tofaa.entitylib", "dev.yyuh.libs.entitylib")
}