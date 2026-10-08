# NexoriaSuite

High-performance, asynchronous server architectures and developer APIs for Paper, Purpur, and Folia servers running on Java 25+.

---

## Overview

NexoriaSuite is a modular ecosystem of 30 plugins designed with a zero-bloat philosophy:

* **Native Folia & Multi-threading**: Automatic environment detection bridging regional schedulers without blocking the main server thread.
* **Adventure MiniMessage**: Dynamic text formatting, gradients, and interactive components.
* **High Concurrency**: Thread-safe collections and non-blocking session registries.

---

## Projects in this Suite

| Project | Summary |
| :--- | :--- |
| **NexoriaCore** | Asynchronous engine and Folia scheduler compatibility bridge. |
| **NexoriaAdventure** | MiniMessage formatting, dynamic gradients, actionbars, and bossbars. |
| **NexoriaMenu** | Reactive inventory GUI builder with automatic pagination. |
| **NexoriaEconomy** | Multi-currency economy engine with Vault bridge. |
| **NexoriaSync** | Real-time Redis cross-server synchronization. |
| **NexoriaDatabase** | HikariCP asynchronous connection pool manager. |
| **NexoriaScoreboard** | Zero-flicker packet-based scoreboard and tablist. |
| **NexoriaChat** | Multi-channel chat formatter with mentions and filters. |
| **NexoriaCombat** | Combat log prevention and floating damage indicators. |
| **NexoriaClaims** | Chunk-based land protection with trust permissions. |
| *(and 20 more modular plugins)* | |

---

## Building from Source

This project uses Gradle with Java 25:

```bash
./gradlew build
```

Compiled `.jar` binaries will be generated inside the `dist/` directory.

---

## License

This project is licensed under the [MIT License](LICENSE).
