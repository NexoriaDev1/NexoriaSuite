# NexoriaSuite — Master Catalog & Modrinth Strategy (30 Plugins/APIs)

> **Reto Objetivo**: Generar un mínimo de **500€** para mejorar tu setup de PC mediante el programa de creadores de Modrinth, donaciones y marketplace.
> **Versión Minecraft Target**: 1.21.x / 26.2 (Paper, Purpur, Folia) con **Java 21+**.

---

## Plan de Monetización Paso a Paso

1. **Modrinth Creator Rewards Program**:
 - Modrinth reparte ingresos publicitarios directamente a creadores según descargas e impresiones de página.
 - **Efecto de Red**: Cada plugin de la saga requiere o recomienda `NexoriaCore` o `NexoriaAdventure`. Al publicar 30 plugins interconectados, un usuario que use 3 plugins tuyos generará 4 a 5 descargas en tu perfil de Modrinth.
 - Con 30 plugins activos y optimizados en SEO/Tags de Modrinth, el tráfico orgánico se multiplica por 30.

2. **Botones de Donación / Monetización Directa**:
 - Configura en tu perfil de Modrinth enlaces directos a **Ko-fi** o **GitHub Sponsors** (0% comisiones).
 - Añade un mensaje en cada descripción invitando a apoyar el desarrollo del ecosistema.

3. **Pack de Configuración Premium (Opcional)**:
 - Los plugins son 100% Open Source en Modrinth (lo que da máxima reputación y descargas).
 - Puedes ofrecer packs de configuraciones prediseñadas (ej. menús pre-hechos para `NexoriaMenu`, misiones listas para `NexoriaQuests`) en plataformas como BuiltByBit.

---

## Catálogo Completo de los 30 Plugins de NexoriaSuite

| # | Nombre del Plugin | Slug Modrinth | Categorías | Resumen / Propósito | Licencia | Plataformas |
| :-: | :--- | :--- | :--- | :--- | :-: | :-: |
| 1 | **NexoriaCore** | `nexoria-core` | API, Utility, Management | Motor async central y abstracción de tareas para Folia/Paper. | MIT | Paper, Purpur, Folia |
| 2 | **NexoriaAdventure** | `nexoria-adventure` | API, Chat, Utility | Parser MiniMessage, gradientes, actionbars y bossbars. | MIT | Paper, Purpur, Folia |
| 3 | **NexoriaMenu** | `nexoria-menu` | API, Utility | Creador de menús e inventarios reactivos con paginación. | MIT | Paper, Purpur, Folia |
| 4 | **NexoriaEconomy** | `nexoria-economy` | Economy, Management | Economía multi-moneda con banco GUI y puente Vault. | MIT | Paper, Purpur, Folia |
| 5 | **NexoriaSync** | `nexoria-sync` | Storage, Utility, Management | Sincronización de inventarios en tiempo real con Redis. | MIT | Paper, Purpur, Folia |
| 6 | **NexoriaDatabase** | `nexoria-database` | API, Storage | Pool de conexiones HikariCP (SQLite, MySQL, MariaDB, Mongo). | MIT | Paper, Purpur, Folia |
| 7 | **NexoriaScoreboard** | `nexoria-scoreboard` | Utility, Chat | Scoreboard y Tablist animados por paquetes sin parpadeos. | MIT | Paper, Purpur, Folia |
| 8 | **NexoriaChat** | `nexoria-chat` | Chat, Management | Chat moderno por canales, menciones, filtros y Discord. | MIT | Paper, Purpur, Folia |
| 9 | **NexoriaCombat** | `nexoria-combat` | Gameplay, Utility | Combat tagger, indicador de daño con hologramas y PvP. | MIT | Paper, Purpur, Folia |
| 10 | **NexoriaClaims** | `nexoria-claims` | Protection, Management | Protección de terrenos por chunks con bordes de partículas. | MIT | Paper, Purpur, Folia |
| 11 | **NexoriaQuests** | `nexoria-quests` | Gameplay, Management | Sistema de misiones diarias/semanales con tracker GUI. | MIT | Paper, Purpur, Folia |
| 12 | **NexoriaNBT** | `nexoria-nbt` | API, Utility | Envoltorio tipo-seguro para PersistentDataContainer (PDC). | MIT | Paper, Purpur, Folia |
| 13 | **NexoriaHolograms** | `nexoria-holograms` | Utility, Management | Hologramas con Display Entities modernos (cero lag de entidades). | MIT | Paper, Purpur, Folia |
| 14 | **NexoriaVanish** | `nexoria-vanish` | Management, Utility | Vanish avanzado para staff con apertura silenciosa de cofres. | MIT | Paper, Purpur, Folia |
| 15 | **NexoriaRTP** | `nexoria-rtp` | Utility, Worldgen | Teletransporte aleatorio asíncrono seguro anti-lag. | MIT | Paper, Purpur, Folia |
| 16 | **NexoriaWarps** | `nexoria-warps` | Utility, Management | Gestor de warps, homes y spawn con selector GUI y warmup. | MIT | Paper, Purpur, Folia |
| 17 | **NexoriaAntiGrief** | `nexoria-antigrief` | Protection, Storage | Logger de bloques y cofres con sistema de rollback ultra-rápido. | MIT | Paper, Purpur, Folia |
| 18 | **NexoriaCustomCraft** | `nexoria-customcraft` | Gameplay, Utility | Recetas de crafteo, herrería y horno personalizadas. | MIT | Paper, Purpur, Folia |
| 19 | **NexoriaSkills** | `nexoria-skills` | Gameplay | Árboles de habilidades RPG con estadísticas y mejoras pasivas. | MIT | Paper, Purpur, Folia |
| 20 | **NexoriaShops** | `nexoria-shops` | Economy, Management | Tiendas en cofres y mercado virtual con precios dinámicos. | MIT | Paper, Purpur, Folia |
| 21 | **NexoriaParticles** | `nexoria-particles` | API, Utility | Animaciones 3D de partículas, alas, auras y estelas. | MIT | Paper, Purpur, Folia |
| 22 | **NexoriaCooldowns** | `nexoria-cooldowns` | API, Utility | API universal de enfriamientos y rate-limit con persistencia. | MIT | Paper, Purpur, Folia |
| 23 | **NexoriaPunish** | `nexoria-punish` | Management, Protection | Suite de sanciones (Baneos, Mutes, Warns) con webhooks. | MIT | Paper, Purpur, Folia |
| 24 | **NexoriaLoot** | `nexoria-loot` | Gameplay, Worldgen | Tablas de loot personalizadas para cofres y drops de mobs. | MIT | Paper, Purpur, Folia |
| 25 | **NexoriaSpawners** | `nexoria-spawners` | Utility, Optimization | Spawners apilables y optimizador de IA para granjas. | MIT | Paper, Purpur, Folia |
| 26 | **NexoriaAnnouncer** | `nexoria-announcer` | Chat, Utility | Anuncios automáticos interactivos con sonido y clics. | MIT | Paper, Purpur, Folia |
| 27 | **NexoriaCustomItems** | `nexoria-customitems` | Gameplay, API | Armas y herramientas con habilidades y efectos únicos. | MIT | Paper, Purpur, Folia |
| 28 | **NexoriaAFK** | `nexoria-afk` | Management, Utility | Detección inteligente de AFK con recompensas en piscinas. | MIT | Paper, Purpur, Folia |
| 29 | **NexoriaWorldGuardBridge**| `nexoria-worldguard-bridge` | API, Protection | Puente universal para banderas y regiones de protección. | MIT | Paper, Purpur, Folia |
| 30 | **NexoriaMetrics** | `nexoria-metrics` | Utility, Optimization | Monitor de TPS, MSPT, memoria y telemetría en tiempo real. | MIT | Paper, Purpur, Folia |

---

## Cómo Publicar un Plugin en Modrinth (Paso a Paso)

1. Ve a [Modrinth Dashboard](https://modrinth.com/dashboard) y haz clic en **"Create Project"**.
2. Selecciona **"Plugin"** como tipo de proyecto.
3. Rellena los datos copiando directamente los valores del archivo `MODRINTH_METADATA.md` que se encuentra dentro de la carpeta de cada plugin.
4. Sube el logo minimalista `icon.svg` incluido en cada carpeta (o conviértelo a PNG si prefieres rasterizado).
5. Selecciona la licencia **MIT** y las etiquetas recomendadas.
6. ¡Listo para publicar y acumular descargas para tu meta de 500€!
