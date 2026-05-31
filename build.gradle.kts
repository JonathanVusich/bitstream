plugins {
    id("java-library")
    id("signing")
    id("com.vanniktech.maven.publish").version("0.36.0")
    id("info.solidsoft.pitest").version("1.19.0")
    id("org.jetbrains.gradle.plugin.idea-ext").version("1.4.1")
}

group = "dev.javax"
version = "0.1.0-RC"

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

tasks.check {
    dependsOn("pitest")
}

// 5. Create a standard Java task to run the benchmarks
tasks.register<JavaExec>("runJmh") {
    description = "Runs JMH benchmarks natively"
    group = "verification"

    // Ensure Gradle compiles the benchmarks before trying to run them
    dependsOn(tasks.named("jmhClasses"))

    mainClass.set("org.openjdk.jmh.Main")
    classpath = sourceSets["jmh"].runtimeClasspath

    // 1. Setup directory for JFR (from previous step)
    val jfrDir = layout.buildDirectory.dir("reports/jfr").get().asFile
    jfrDir.mkdirs()

    // 2. Setup directory for JITWatch logs
    val jitDir = layout.buildDirectory.dir("reports/jit").get().asFile
    jitDir.mkdirs()

    // 3. Compile the exact JVM arguments JITWatch requires
    val jitJvmArgs = listOf(
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:+LogCompilation",
        "-XX:LogFile=${jitDir.absolutePath}/jit_compilation.log"
    ).joinToString(" ")

    // 4. Pass everything to JMH
    args(
        "-bm", "thrpt",
        "-rf", "json",
        "-prof", "jfr:dir=${jfrDir.absolutePath}", // Your JFR setup
        "-jvmArgsAppend", jitJvmArgs               // Pass the JIT flags to the forks
    )
}


pitest {
    targetClasses = setOf("dev.javax.*")
    threads = 4
    outputFormats = setOf("HTML")
    timestampedReports = false
    junit5PluginVersion = "1.2.1"
    pitestVersion = "1.19.0"
    mutationThreshold = 100
    jvmArgs = listOf("-Xmx2048m")
}

tasks.test {
    useJUnitPlatform()
}

signing {
    useGpgCmd()
}

mavenPublishing {
    // Targets the new Central Portal API and automatically drops snapshots/releases
    publishToMavenCentral()

    // Automatically applies the Gradle signing plugin and signs all artifacts
    signAllPublications()

    // Standard Maven POM requirements
    pom {
        name.set("bitstream")
        description.set("Bit manipulation streams for low level encodings")
        url.set("https://github.com/JonathanVusich/bitstream")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("JonathanVusich")
                name.set("Jonathan Vusich")
                email.set("jonathan@vusich.cloud")
            }
        }

        scm {
            url.set("https://github.com/JonathanVusich/bitstream")
            connection.set("scm:git:git://github.com/JonathanVusich/bitstream.git")
            developerConnection.set("scm:git:ssh://git@github.com/JonathanVusich/bitstream.git")
        }
    }
}