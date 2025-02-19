<h1 style="text-align:center;">ThreadUtil</h1>

<p style="text-align:center;">
    <a href="https://github.com/milkdrinkers/ThreadUtil/blob/main/LICENSE">
        <img alt="GitHub License" src="https://img.shields.io/github/license/milkdrinkers/ThreadUtil?style=for-the-badge&color=blue&labelColor=141417">
    </a>
    <a href="https://central.sonatype.com/artifact/io.github.milkdrinkers/threadutil-common">
        <img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/io.github.milkdrinkers/threadutil-common?style=for-the-badge&labelColor=141417">
    </a>
    <a href="https://javadoc.io/doc/io.github.milkdrinkers/threadutil-common">
        <img alt="Javadoc" src="https://img.shields.io/badge/JAVADOC-8A2BE2?style=for-the-badge&labelColor=141417">
    </a>
    <img alt="GitHub Actions Workflow Status" src="https://img.shields.io/github/actions/workflow/status/milkdrinkers/ThreadUtil/ci.yml?style=for-the-badge&labelColor=141417">
    <a href="https://github.com/milkdrinkers/ThreadUtil/issues">
        <img alt="GitHub Issues" src="https://img.shields.io/github/issues/milkdrinkers/ThreadUtil?style=for-the-badge&labelColor=141417">
    </a>
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/milkdrinkers/ThreadUtil?style=for-the-badge&labelColor=141417">
</p>

A fluent scheduling utility for Minecraft plugins, providing elegant async/sync runnable chaining with Bukkit/Spigot/Paper/Velocity integration.

---

## 🌟 Features
- 🧵 Fluent API for async/sync task chaining
- ⏱️ Built-in delay support with tick/Duration precision
- 🚫 Stage cancellation support
- 🔒 Thread-safe error handling
- 🧩 Custom thread pool integration

## 📦 Installation

The `bukkit` & `velocity` modules depend on `common`. Additionally you should shade the dependency into your plugin jar.

<details>
<summary>Gradle Kotlin DSL</summary>

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.milkdrinkers:threadutil-common:1.0.0")
    implementation("io.github.milkdrinkers:threadutil-bukkit:1.0.0")
    implementation("io.github.milkdrinkers:threadutil-velocity:1.0.0")
}
```
</details>

<details>
<summary>Maven</summary>

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>io.github.milkdrinkers</groupId>
            <artifactId>threadutil-common</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>io.github.milkdrinkers</groupId>
            <artifactId>threadutil-bukkit</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>io.github.milkdrinkers</groupId>
            <artifactId>threadutil-velocity</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</project>
```
</details>

## Usage Example 🚀
```java
import io.github.milkdrinkers.threadutil.Scheduler;

Scheduler.async(() -> {
    // Async database operation
    return fetchPlayerData(player.getUniqueId());
})
.delay(Duration.ofSeconds(1))
.delay(1) // Wait one game tick on supported platforms
.sync(data -> {
    // Sync UI update
    player.sendMessage("Loaded: " + data.toString());
    return data.process();
})
.async(processed -> {
    // Async file I/O
    saveToFile(processed);
    return processed;
})
.execute();
```

## 📚 Documentation 

- [Full Javadoc Documentation](https://javadoc.io/doc/io.github.milkdrinkers/threadutil-common)
- [Documentation](https://milkdrinkers.github.io/)
- [Maven Central](https://central.sonatype.com/artifact/io.github.milkdrinkers/threadutil-common)

---

## 🔨 Building from Source 

```bash
git clone https://github.com/milkdrinkers/ThreadUtil.git
cd threadutil
./gradlew publishToMavenLocal
```

---

## 🔧 Contributing

Contributions are always welcome! Please make sure to read our [Contributor's Guide](CONTRIBUTING.md) for standards and our [Contributor License Agreement (CLA)](CONTRIBUTOR_LICENSE_AGREEMENT.md) before submitting any pull requests.

We also ask that you adhere to our [Contributor Code of Conduct](CODE_OF_CONDUCT.md) to ensure this community remains a place where all feel welcome to participate.

---

## 📝 Licensing

You can find the license the source code and all assets are under [here](../LICENSE). Additionally, contributors agree to the Contributor License Agreement \(*CLA*\) found [here](CONTRIBUTOR_LICENSE_AGREEMENT.md).

---

## ❤️ Acknowledgments

- **[Aikar:](https://github.com/aikar)** _For their excellent utility [__TaskChain__](https://github.com/aikar/TaskChain/), which this was inspired by. I highly recommend their library, providing the same features and MUCH MORE for any platform._
