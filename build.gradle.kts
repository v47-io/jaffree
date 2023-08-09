import name.remal.gradle_plugins.plugins.publish.ossrh.RepositoryHandlerOssrhExtension
import org.gradle.api.JavaVersion.VERSION_17
import java.util.*

plugins {
    alias(libs.plugins.kotlinJvm)
    checkstyle
    jacoco
    // pmd

    alias(libs.plugins.license)

    alias(libs.plugins.release)
    `maven-publish`
    alias(libs.plugins.mavenPublishOssrh) apply false
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

java.sourceCompatibility = VERSION_17
java.targetCompatibility = VERSION_17

kotlin {
    jvmToolchain(17)
}

tasks.compileKotlin {
    destinationDirectory.set(tasks.compileJava.get().destinationDirectory)
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
    skipExistingHeaders = false

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
    toolVersion = "0.8.10"
}

/*pmd {
    maxFailures = 0
}*/

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

            artifact(tasks.javadoc) {
                classifier = "javadoc"
            }

            pom {
                name.set("Jaffree")
                description.set("Java ffmpeg and ffprobe command-line wrapper ")
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
    apply(plugin = libs.plugins.mavenPublishOssrh.get().pluginId)

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
