import name.remal.gradle_plugins.plugins.publish.ossrh.RepositoryHandlerOssrhExtension
import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.Platform
import org.jetbrains.dokka.gradle.DokkaTask
import java.util.*

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(libs.remalGradlePlugins)
    }
}

plugins {
    alias(libs.plugins.kotlinJvm)
    checkstyle
    jacoco
    // pmd

    alias(libs.plugins.license)

    alias(libs.plugins.dokka)
    alias(libs.plugins.release)
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.annotations)

    implementation(libs.nanojson)
    implementation(libs.nuprocess)
    implementation(libs.slf4j)

    testImplementation(libs.commonsIo)
    testImplementation(libs.junitApi)
    testImplementation(libs.junitParams)

    testRuntimeOnly(libs.logback)
    testRuntimeOnly(libs.junitEngine)
}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}

license {
    excludePatterns = setOf(
        "**/*.flat",
        "**/*.gif",
        "**/*.java",
        "**/*.json",
        "**/*.log",
        "**/*.mp4",
        "**/*.txt",
        "**/*.xml"
    )

    header = file("HEADER.txt")
    skipExistingHeaders = true

    ext {
        set("year", Calendar.getInstance().get(Calendar.YEAR))
    }
}

release {
    tagTemplate.set("v\$version")
}

checkstyle {
    configFile = file("$projectDir/checkstyle.xml")
}

tasks.checkstyleTest.get().enabled = false

jacoco {
    toolVersion = "0.8.12"
}

/*pmd {
    maxFailures = 0
}*/

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        configureEach {
            documentedVisibilities.set(setOf(DokkaConfiguration.Visibility.PUBLIC))
            jdkVersion.set(11)
            includes.from(project.files(), "packages.md")
            platform.set(Platform.jvm)
        }
    }
}

val dokkaJavadoc = tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create("maven", MavenPublication::class) {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            from(components.getByName("java"))

            artifact(sourcesJar) {
                classifier = "sources"
            }

            artifact(dokkaJavadoc) {
                classifier = "javadoc"
            }

            pom {
                name.set("Jaffree")
                description.set("Java ffmpeg and ffprobe command-line wrapper")
                url.set("https://github.com/v47-io/jaffree")

                licenses {
                    license {
                        name.set("GNU General Public License version 3")
                        url.set("https://opensource.org/license/gpl-3-0/")
                    }
                }

                developers {
                    developer {
                        id.set("vemilyus")
                        name.set("Alex Katlein")
                        email.set("dev@vemilyus.com")
                        url.set("https://v47.io")
                    }

                    developer {
                        id.set("kokorin")
                        name.set("Denis Kokorin")
                        email.set("kokorin86@gmail.com")
                        url.set("https://github.com/kokorin")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/v47-io/jaffree.git")
                    developerConnection.set("scm:git:git://github.com/v47-io/jaffree.git")
                    url.set("https://github.com/v47-io/jaffree")
                }
            }
        }
    }
}

val ossrhUser: String? = project.findProperty("ossrhUser") as? String ?: System.getenv("OSSRH_USER")
val ossrhPass: String? = project.findProperty("osshrPass") as? String ?: System.getenv("OSSRH_PASS")

if (!ossrhUser.isNullOrBlank() && !ossrhPass.isNullOrBlank() && !"${project.version}".endsWith("-SNAPSHOT")) {
    apply(plugin = "signing")
    apply(plugin = "name.remal.maven-publish-ossrh")

    publishing {
        repositories {
            @Suppress("DEPRECATION")
            withConvention(RepositoryHandlerOssrhExtension::class) {
                ossrh {
                    credentials {
                        username = ossrhUser
                        password = ossrhPass
                    }
                }
            }
        }
    }
}
