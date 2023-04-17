import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    `maven-publish`
    kotlin("jvm") version "1.8.10"
    id("org.openjfx.javafxplugin") version "0.0.13"
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
    implementation("com.github.walkyst:lavaplayer-fork:1.4.0")
    implementation("com.adamratzman:spotify-api-kotlin-core:4.0.0")
    implementation("com.vdurmont:emoji-java:5.1.1")
    implementation("com.dustinredmond.fxtrayicon:FXTrayIcon:4.0.1")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.6")
    runtimeOnly("org.fusesource.jansi:jansi:1.17")

    //Fix vulnerabilities
    runtimeOnly("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    runtimeOnly("commons-codec:commons-codec:1.15")
    runtimeOnly("org.apache.httpcomponents:httpclient:4.5.14")
    runtimeOnly("org.json:json:20230227")

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

tasks.withType<ShadowJar> {
    //archiveFileName.set("${project.name}-${project.version}-all.jar")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<JavaCompile> {
    targetCompatibility = "17"
}

publishing.publications {
    create<MavenPublication>("maven") {
        groupId = project.group as String
        artifactId = project.name
        version = project.version as String

        from(components["java"])
    }
}
