plugins {
    java
    id ("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.bluecolored.bluemap.lzma2"
version = "1.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven ( "https://repo.bluecolored.de/releases" )
}

dependencies {
    compileOnly ( "de.bluecolored.bluemap:BlueMapCore:5.2" )

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

val compileNative by tasks.creating(Exec::class) {
    val javaHome = System.getProperty("java.home")
    environment("JAVA_HOME", javaHome)
    workingDir("src/main/c")
    commandLine("make")

    inputs.file("src/main/c/native_lzma.c")
    inputs.file("src/main/c/Makefile")
    outputs.file("src/main/c/libnative_lzma.so")

    doLast {
        val lib = file("src/main/c/libnative_lzma.so")
        val destDir = file("src/main/resources/native/linux-x86_64")
        destDir.mkdirs()
        lib.copyTo(destDir.resolve("libnative_lzma.so"), overwrite = true)
    }
}

tasks.processResources {
    dependsOn(compileNative)
}

tasks.shadowJar {
    doLast {
        val destFile = projectDir.resolve("build/release/bluemap-lzma2-$version.jar")
        destFile.parentFile.mkdirs()
        destinationDirectory.file(archiveFileName).get().asFile
            .copyTo(destFile, overwrite = true)
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "utf-8"
}

tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
