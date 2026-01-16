plugins {
    id("java")
    alias(libs.plugins.shadow)
}

group = "club.revived.limbo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.loohpjames.com/repository") // Limbo Plugin API
}

dependencies {
    implementation(libs.limbo)
    implementation(libs.jedis)
    implementation(libs.adventure)
    implementation(project(":commons"))
}