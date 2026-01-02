plugins {
    id("java")
    alias(libs.plugins.paperweight)
}

group = "club.revived.commons"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper)
}

tasks.test {
    useJUnitPlatform()
}