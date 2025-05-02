import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI
import java.util.*
import java.util.Calendar.YEAR

plugins {
    alias(libs.plugins.kotlinJvm)
    checkstyle
    jacoco
    // pmd

    alias(libs.plugins.license)

    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.release)
    alias(libs.plugins.ncmp.maven.publish)
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

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitApi)
    testImplementation(libs.junitParams)

    testRuntimeOnly(libs.logback)
    testRuntimeOnly(libs.junitEngine)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

kotlin {
    jvmToolchain(11)
}

tasks.compileJava {
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        listOf("--patch-module", "com.github.kokorin.jaffree=${sourceSets["main"].output.asPath}")
    })
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

dokka {
    dokkaSourceSets.main {
        includes.from("packages.md")
        documentedVisibilities.set(setOf(VisibilityModifier.Public))

        val revision = "${project.version}".let { version ->
            if (version.endsWith("-SNAPSHOT"))
                "main"
            else
                "v$version"
        }

        sourceLink {
            localDirectory = file("src/main/java")
            remoteUrl = URI("https://github.com/v47-io/jaffree/blob/$revision/src/main/java")
            remoteLineSuffix = "#L"
        }

        sourceLink {
            localDirectory = file("src/main/kotlin")
            remoteUrl = URI("https://github.com/v47-io/jaffree/blob/$revision/src/main/kotlin")
            remoteLineSuffix = "#L"
        }
    }

    pluginsConfiguration {
        val copyright = "Copyright (c) ${Calendar.getInstance().get(YEAR)} jaffree authors"

        html {
            footerMessage = copyright
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("${project.group}", project.name, "${project.version}")

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

            developer {
                id.set("jonfryd")
                name.set("Jon Frydensbjerg")
                email.set("jonf@vip.cybercity.dk")
                url.set("https://github.com/jonfryd")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/v47-io/jaffree.git")
            developerConnection.set("scm:git:git://github.com/v47-io/jaffree.git")
            url.set("https://github.com/v47-io/jaffree")
        }
    }
}
