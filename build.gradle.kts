plugins {
    id("java-library")
    id("info.solidsoft.pitest").version("1.19.0")
}

group = "org.bitstream"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // https://mvnrepository.com/artifact/org.assertj/assertj-core
    testImplementation("org.assertj:assertj-core:3.27.7")
    // Source: https://mvnrepository.com/artifact/org.apache.commons/commons-compress
    testImplementation("org.apache.commons:commons-compress:1.28.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

pitest {
    targetClasses = setOf<String>("org.bitstream.*")
    threads = 4
    outputFormats = setOf<String>("HTML")
    timestampedReports = true
    junit5PluginVersion = "1.2.1"
    pitestVersion = "1.19.0"
    jvmArgs = listOf<String>("-Xmx2048m")
}

tasks.test {
    useJUnitPlatform()
}