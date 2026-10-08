# NexoriaWorldGuardBridge

> **Seamless custom flag registration and region integration bridge for WorldGuard and Paper/Folia.**

---

### Modrinth Project Details

| Parameter | Specification |
| :--- | :--- |
| **Title** | `NexoriaWorldGuardBridge` |
| **Project Slug** | `nexoria-worldguard-bridge` |
| **Short Summary** | `Seamless custom flag registration and region integration bridge for WorldGuard and Paper/Folia.` |
| **Loaders / Platforms** | `Paper`, `Purpur`, `Folia` |
| **Environment** | `Server` |
| **License** | `MIT` |
| **Categories** | `api-and-library, protection` |
| **Java Requirement** | `Java 25+` (or modern OpenJDK runtime) |

---

### Overview & Capabilities

**NexoriaWorldGuardBridge** is engineered from the ground up to deliver uncompromising performance and seamless concurrency on modern multi-threaded Minecraft server architectures like Folia and Paper.

* **High Concurrency**: Built with thread-safe non-blocking registries and async data delivery.
* **Folia Ready**: Native multi-threaded region scheduler integration without main-thread stalling.
* **Zero Dependencies**: Lightweight binary size with instant startup and reload capabilities.

---

### Developer Integration (Gradle Kotlin DSL)

```kotlin
repositories {
 maven("https://api.modrinth.com/maven")
}

dependencies {
 compileOnly("maven.modrinth:nexoria-worldguard-bridge:1.0.0")
}
```

#### Java 25 API Usage Example

```java
WorldGuardBridgeAPI api = NexoriaWorldGuardBridgePlugin.getInstance().getApi();

if (api.isActive()) {
 var health = api.getStatus();
 System.out.println("Service status: " + health.service() + " is active at " + health.timestamp());
}
```

---

### Support the Ecosystem
If you find this plugin valuable for your network, consider supporting our ongoing development on Ko-fi or starring our project!
