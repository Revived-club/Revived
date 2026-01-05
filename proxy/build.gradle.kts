plugins {
    id("java")
    alias(libs.plugins.shadow)
}

group = "dev.yyuh.proxy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
    implementation(project(":commons"))
    implementation(libs.jedis)
}

tasks.test {
    useJUnitPlatform()
}