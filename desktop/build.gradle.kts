import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.0.0"
}

group = "me.sett"
version = "1.0"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
            exec {
                commandLine(
                    "dlltool",
                    "-d",
                    "lib/extra.def",
                    "-l",
                    "lib/extra.lib",
                )
            }
            exec {
                commandLine(
                    "gcc",
                    ".\\lib\\synthPointer.c",
                    ".\\lib\\extra.lib",
                    "-o",
                    ".\\lib\\libSynthPointer.so",
                    "-shared",
                    "-fPIC",
                    "-IC:/Users/sett/.jdks/openjdk-17.0.1/include",
                    "-IC:/Users/sett/.jdks/openjdk-17.0.1/include/win32"
                )
            }
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jvm"
            packageVersion = "1.0.0"
        }
    }
}
