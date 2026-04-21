plugins {
    id("java-library")
    id("info.solidsoft.pitest").version("1.19.0")
//    id("me.champeau.jmh").version("0.7.3")
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

// 2. Register a native SourceSet for your benchmarks (src/jmh/java)
sourceSets {
    create("jmh") {
        java {
            srcDirs("src/jmh/java")
        }
        // Give JMH access to your main source code
        compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath
    }
}

// 1. Tell Kotlin DSL to fetch the dynamically created configurations
val jmhImplementation by configurations.existing
val jmhAnnotationProcessor by configurations.existing

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // https://mvnrepository.com/artifact/org.assertj/assertj-core
    testImplementation("org.assertj:assertj-core:3.27.7")
    // Source: https://mvnrepository.com/artifact/org.apache.commons/commons-compress
    testImplementation("org.apache.commons:commons-compress:1.28.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // 3. Add your specific JMH library dependencies
    jmhImplementation("org.apache.commons:commons-compress:1.28.0")

    // 4. Add the official JMH Core and Annotation Processor directly
    val jmhVersion = "1.37"
    jmhImplementation("org.openjdk.jmh:jmh-core:$jmhVersion")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
}

// 5. Create a standard Java task to run the benchmarks
tasks.register<JavaExec>("runJmh") {
    description = "Runs JMH benchmarks natively"
    group = "verification"

    // Ensure Gradle compiles the benchmarks before trying to run them
    dependsOn(tasks.named("jmhClasses"))

    mainClass.set("org.openjdk.jmh.Main")
    classpath = sourceSets["jmh"].runtimeClasspath

    // Pass your default JMH arguments here (equivalent to what was in your jmh { } block)
    args("-bm", "thrpt,ss", "-rf", "json")
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