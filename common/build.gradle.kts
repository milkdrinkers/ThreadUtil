plugins {
    `maven-publish`
    signing
}

dependencies {
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
            artifactId = "threadutil-common"
            version = "${rootProject.version}"

            pom {
                name.set(rootProject.name)
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
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = findProperty("MAVEN_USERNAME")?.toString() ?: System.getenv("MAVEN_USERNAME")
                password = findProperty("MAVEN_PASSWORD")?.toString() ?: System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    isRequired = !rootProject.version.toString().contains("SNAPSHOT") // Only require signing when publishing releases

    val signingKey = findProperty("GPG_KEY")?.toString() ?: System.getenv("GPG_KEY")
    val signingPassword = findProperty("GPG_PASSWORD")?.toString() ?: System.getenv("GPG_PASSWORD")

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}
