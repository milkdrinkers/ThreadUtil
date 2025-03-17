plugins {
    alias(libs.plugins.maven.deployer)
}

dependencies {
    api(projects.common)
    compileOnly(libs.paper.api)
}

tasks {
    test {
        useJUnitPlatform()
        failFast = false
    }
}

deployer {
    release {
        version.set("${rootProject.version}")
        description.set(rootProject.description.orEmpty())
    }

    projectInfo {
        groupId = "io.github.milkdrinkers"
        artifactId = "threadutil-bukkit"
        version = "${rootProject.version}"

        name = rootProject.name + "-Bukkit"
        description = rootProject.description.orEmpty()
        url = "https://github.com/milkdrinkers/ThreadUtil"

        scm {
            connection = "scm:git:git://github.com/milkdrinkers/ThreadUtil.git"
            developerConnection = "scm:git:ssh://github.com:milkdrinkers/ThreadUtil.git"
            url = "https://github.com/milkdrinkers/ThreadUtil"
        }

        license("MIT License", "https://opensource.org/licenses/MIT")

        developer({
            name.set("darksaid98")
            email.set("darksaid9889@gmail.com")
            url.set("https://github.com/darksaid98")
            organization.set("Milkdrinkers")
        })
    }

    content {
        component {
            fromJava()
        }
    }

    centralPortalSpec {
        auth.user.set(secret("MAVEN_USERNAME"))
        auth.password.set(secret("MAVEN_PASSWORD"))
    }

    signing {
        key.set(secret("GPG_KEY"))
        password.set(secret("GPG_PASSWORD"))
    }
}
