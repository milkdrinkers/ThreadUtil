plugins {
    `maven-publish`
    signing
}

dependencies {
    api(projects.common)
    annotationProcessor(libs.velocity.api)
    compileOnly(libs.velocity.api)
}

tasks {
    test {
        useJUnitPlatform()
        failFast = false
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "io.github.milkdrinkers"
            artifactId = "threadutil-velocity"
            version = "${rootProject.version}"

            pom {
                name.set(rootProject.name + "-Velocity")
                description.set(rootProject.description.orEmpty())
                url.set("https://github.com/milkdrinkers/ThreadUtil")

                licenses {
                    license {
                        name.set("GNU General Public License Version 3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
                    }
                }

                developers {
                    developer {
                        id.set("darksaid98")
                        name.set("darksaid98")
                        email.set("darksaid9889@gmail.com")
                        url.set("https://github.com/darksaid98")
                        organization.set("Milkdrinkers")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/milkdrinkers/ThreadUtil.git")
                    developerConnection.set("scm:git:ssh://github.com:milkdrinkers/ThreadUtil.git")
                    url.set("https://github.com/milkdrinkers/ThreadUtil")
                }
            }
        }
    }

    repositories {
        maven {
            name = "CentralPortal"
            url = uri("https://central.sonatype.com/api/v1/publisher")

            credentials {
                username = findProperty("MAVEN_USERNAME")?.toString() ?: System.getenv("MAVEN_USERNAME")
                password = findProperty("MAVEN_PASSWORD")?.toString() ?: System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = findProperty("GPG_KEY")?.toString() ?: System.getenv("GPG_KEY")
    val signingPassword = findProperty("GPG_PASSWORD")?.toString() ?: System.getenv("GPG_PASSWORD")

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}
