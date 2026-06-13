plugins {
    java
    id ("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.bluecolored.bluemap.lzma2"
version = "1.0"

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

    implementation ( "org.tukaani:xz:1.10" )
}

tasks.shadowJar {
    relocate( "org.tukaani.xz", "de.bluecolored.shadow.xz" )

    doLast {
        destinationDirectory.file(archiveFileName).get().asFile
            .copyTo(projectDir.resolve("build/release/bluemap-lzma2-$version.jar"))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "utf-8"
}

tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
