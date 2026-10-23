# NexoriaSuite

High-performance, modular Minecraft server plugin ecosystem built for modern Paper, Purpur, and Folia architectures running on Java 25 LTS.

---

## Active Modules

### 1. NexoriaCore
- **Type**: Core Framework & Shared Library
- **Path**: `nexoria-core/`
- **Description**: Central service registry and Folia-compatible task scheduling bridge. Provides foundational utilities for the Nexoria ecosystem.

### 2. NexoriaEconomy
- **Type**: Currency & Economy Engine
- **Path**: `nexoria-economy/`
- **Description**: Standalone economy provider with native Vault registration, transactional safety, paginated leaderboard (`/baltop`), and administrative tools.
- **Dependencies**: [Vault](https://www.spigotmc.org/resources/vault.34315/)

---

## Upcoming Releases

The following modules are currently in development and testing:
- **NexoriaShops**: Interactive chest and GUI merchant shops integrated with Vault.
- Additional gameplay, protection, and utility modules will be published progressively.

---

## Building from Source

This project uses Gradle with Java 25 toolchains.

```bash
# Clone the repository
git clone https://github.com/NexoriaDev1/NexoriaSuite.git
cd NexoriaSuite

# Build active modules
./gradlew build
```

Compiled `.jar` binaries are output to each module's `build/libs/` directory.

---

## License

This project is licensed under the [MIT License](LICENSE).
