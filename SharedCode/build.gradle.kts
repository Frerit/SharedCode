import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.util.Date

buildscript {
    repositories {
        jcenter()
        google()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    }

    dependencies {
        val kotlinVersion = "1.3.61"
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath(kotlin("serialization", version = kotlinVersion))
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    }
}

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.3.61"
    kotlin("native.cocoapods")
    id("com.jfrog.bintray") version "1.8.4"

    id("com.codingfeline.buildkonfig")
    id("maven-publish")
}

apply(plugin = "kotlinx-serialization")
apply(plugin = "com.codingfeline.buildkonfig")
apply(plugin = "com.jfrog.bintray")
apply(from = "./props.gradle")

val groupVersion = "0.1.8"
val ktorVersion = "1.3.1"
val coroutineVersion = "1.1.1"
val serializerVersion = "0.14.0"

val artifactName = project.name
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()

val pomUrl = "https://github.com/serpro69/kotlin-faker"
val pomScmUrl = "https://github.com/serpro69/kotlin-faker"
val pomIssueUrl = "https://github.com/serpro69/kotlin-faker/issues"
val pomDesc = "https://github.com/serpro69/kotlin-faker"

group = "app.shared.appcompania"
version = groupVersion

kotlin {
    //select iOS target platform depending on the Xcode environment variables

    val buildForDevice = project.findProperty("kotlin.native.cocoapods.target") == "ios_arm"
    if (buildForDevice) {
        iosArm64("iOS64")
        iosArm32("iOS32")

        val iOSMain by sourceSets.creating
        sourceSets["iOS64Main"].dependsOn(iOSMain)
        sourceSets["iOS32Main"].dependsOn(iOSMain)
    } else {
        iosX64("ios")
    }

    jvm("android")

    cocoapods {
        summary = "Working from Kotlin/Native using CocoaPods"
        homepage = "https://github.com/JetBrains/kotlin-native"
        authors = "Pragma S.A."
    }

    sourceSets {
         commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-json-native:$ktorVersion")
                // COROUTINES
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializerVersion")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation(kotlin("stdlib-common"))
                implementation("io.ktor:ktor-client-android:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializerVersion")
            }
        }

        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
                implementation("io.ktor:ktor-client-ios-iosx64:$ktorVersion")
                implementation("io.ktor:ktor-client-json-native:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-native:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializerVersion")
            }
        }
    }
}

val sources by tasks.creating(Jar::class) {
    from(sourceSets)
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets)
}
val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    from(sourceSets)
}

publishing {
    publications {
        repositories {
            maven {
                val user = "freritmg12"
                val repo = "NativoShared"
                val name = "share-code"
                url = uri("https://api.bintray.com/maven/$user/$repo/$name/;publish=0")

                credentials {
                    username = project.findProperty("bintrayUser").toString()
                    password = project.findProperty("bintrayKey").toString()
                }
            }
        }

        create<MavenPublication>("nativo") {
            artifactId = "share-code"
            groupId = "com.shared.appcompania"
            version = groupVersion
            from(components["kotlin"])

            artifact(sources)
            artifact(sourcesJar)
            artifact(javadocJar)

            pom.withXml {
                asNode().apply {
                    appendNode("description", pomDesc)
                    appendNode("name", rootProject.name)
                    appendNode("url", pomUrl)
                    appendNode("scm").apply {
                        appendNode("url", pomScmUrl)
                    }
                }
            }
        }
    }
}

bintray {
    user = project.findProperty("bintrayUser").toString()
    key = project.findProperty("bintrayKey").toString()
    publish = true
    override = true
    setPublications("nativo")

    pkg.apply {
        repo = "NativoShared"
        name = "share-code"
        userOrg = "freritmg12"
        vcsUrl = pomScmUrl
        description = "Shared Exito"
        setLicenses("Apache-2.0")
        publicDownloadNumbers = true
        desc = description

        version.apply {
            name = groupVersion
            desc = pomDesc
            released = Date().toString()
            vcsTag = groupVersion
        }
    }
}


val packForXcode by tasks.creating(Sync::class) {
    group = "build"

    //selecting the right configuration for the iOS framework depending on the Xcode environment variables
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>("ios").binaries.getFramework(mode)

    inputs.property("mode", mode)
    dependsOn(framework.linkTask)

    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)

    doLast {
        val gradlew = File(targetDir, "gradlew")
        gradlew.writeText("#!/bin/bash\nexport 'JAVA_HOME=${System.getProperty("java.home")}'\ncd '${rootProject.rootDir}'\n./gradlew \$@\n")
        gradlew.setExecutable(true)
    }
}
