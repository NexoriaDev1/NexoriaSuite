# NexoriaSuite

Modular, high-performance plugin ecosystem and developer APIs for Paper, Purpur, and Folia servers running on Java 25+.

---

## Ecosystem Modules

### 1. NexoriaCore
* **Type**: Core Foundation / Library
* **Description**: Asynchronous task framework and Folia multi-threaded regional scheduler bridge.
* **Module Path**: `nexoria-core/`

### 2. NexoriaEconomy
* **Type**: Economy Engine
* **Description**: High-throughput multi-currency system with native Vault provider support and atomic transaction safety.
* **Module Path**: `nexoria-economy/`
* **Dependencies**: Requires `NexoriaCore`.

---

## Building from Source

This project uses Gradle with Java 25 toolchains.

```bash
./gradlew build
```

Compiled `.jar` binaries will be output to their respective build directories.

---

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. Redistribution and modifications require attribution and must remain open source under the same license terms.
