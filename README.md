# NexoriaCore

Asynchronous foundation, multi-threaded regional scheduler bridge, and developer utility API for modern Paper, Purpur, and Folia Minecraft servers running on Java 25+.

---

## Features

* **Multi-threaded Folia & Paper Support**: Automatic environment detection bridging regional schedulers without blocking the main server thread.
* **Java 25 Architecture**: Built using immutable records and high-throughput concurrent session registries.
* **Non-blocking Concurrency**: Non-blocking player session tracking and event handling.
* **Developer API**: Clean programmatic endpoints for runtime health checks and asynchronous task execution.

---

## Commands & Permissions

* `/core status` — Displays runtime health and active session counters.
* `/core reload` — Reloads configuration settings in real-time.
  * Admin node: `nexoria.core.admin` (Default: OP)
  * Player node: `nexoria.core.use` (Default: Everyone)

---

## Developer API Integration

### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven("https://api.modrinth.com/maven")
}

dependencies {
    compileOnly("maven.modrinth:nexoria-core:1.0.0")
}
```

### Java Usage
```java
NexoriaCoreAPI api = NexoriaCorePlugin.getInstance().getApi();

if (api.isActive()) {
    var health = api.getStatus();
    System.out.println("NexoriaCore running: " + health.service());
}
```

---

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. Anyone who modifies or redistributes this software must provide attribution, keep it open-source, and distribute under the same license terms.
