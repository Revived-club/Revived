plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}


tasks.register<Exec>("dev") {
    dependsOn(
        ":duels:build",
        ":lobby:build",
        ":proxy:build",
        ":service:queue:build"
    )

    commandLine("docker", "compose", "up", "-d", "--build")
}