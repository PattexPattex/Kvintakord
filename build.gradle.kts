import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    `maven-publish`
    kotlin("jvm") version "1.9.10"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val appMainClass = "com.pattexpattex.kvintakord.app.Launcher"
group = "com.pattexpattex"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

javafx {
    version = "20"
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.swing")
}

dependencies {
    implementation("com.github.edvin:tornadofx2:master-SNAPSHOT")
    implementation("dev.arbjerg:lavaplayer:2.0.1")
    implementation("com.adamratzman:spotify-api-kotlin-core:4.0.2")
    implementation("com.vdurmont:emoji-java:5.1.1")
    implementation("com.dustinredmond.fxtrayicon:FXTrayIcon:4.0.1")
    implementation("org.slf4j:jul-to-slf4j:2.0.9")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.11")
    runtimeOnly("org.fusesource.jansi:jansi:2.4.0")

    //Fix vulnerabilities
    runtimeOnly("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    runtimeOnly("commons-codec:commons-codec:1.16.0")
    runtimeOnly("org.apache.httpcomponents:httpclient:4.5.14")
    runtimeOnly("org.json:json:20230618")

    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:win")
    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:linux")
    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:mac")
    runtimeOnly("org.openjfx:javafx-controls:${javafx.version}:win")
    runtimeOnly("org.openjfx:javafx-controls:${javafx.version}:linux")
    runtimeOnly("org.openjfx:javafx-controls:${javafx.version}:mac")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set(appMainClass)
}

tasks.distTar {
    val path = archivePath
    archiveVersion.set("")

    doLast {
        archivePath.renameTo(path)
    }
}

tasks.distZip {
    val path = archivePath
    archiveVersion.set("")

    doLast {
        archivePath.renameTo(path)
    }
}

distributions.main {
    contents {
        from("assets/icons/icon.png")
        from("assets/icons/icon.ico")
        from("LICENSE")
    }
}

tasks.withType<CreateStartScripts> {
    (unixStartScriptGenerator as TemplateBasedScriptGenerator).template = resources.text.fromFile("assets/scripts/unixStartScript.txt")
    (windowsStartScriptGenerator as TemplateBasedScriptGenerator).template = resources.text.fromFile("assets/scripts/windowsStartScript.txt")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to appMainClass,
            "Implementation-Version" to project.version,
            "Implementation-Title" to project.name,
            "Implementation-Vendor-Id" to project.group
        )
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<JavaCompile> {
    targetCompatibility = "17"
}
