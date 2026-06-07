# BadCompany Modmenu

BadCompany is a Minecraft `1.21.11` through `1.21.x` Fabric client utility mod. The post-migration codebase is Java 21-only, uses Yarn/Fabric APIs directly, and keeps the old Forge/MCP source tree isolated under `legacy/` for historical reference only; it is not part of the Gradle build.

## Target Platform

| Component | Version |
| --- | --- |
| Minecraft Java | `>=1.21.11 <1.22` |
| Fabric Loader | `0.19.3` |
| Fabric API | `0.141.4+1.21.11` |
| Fabric Loom | `1.14.8` |
| Yarn mappings | `1.21.11+build.6` |
| Java | `21` |

## Build Instructions

```bash
./gradlew build
```

The compiled mod jar is written to `build/libs/`. Use a Java 21 JDK; older Java runtimes are unsupported. The lower bound is Minecraft 1.21.11 because the active ClickGUI uses the 1.21.11+ client input APIs (`Click`, `KeyInput`, and `CharInput`), so 1.21.4 and earlier are not declared compatible.

## Development Setup

1. Install a Java 21 JDK.
2. Clone the repository.
3. Run `./gradlew build` to compile and remap the client jar.
4. Run `./gradlew runClient` to launch the Fabric development client.
5. Open the GUI in-game with the default backslash (`\`) keybind. The keybind can be changed in Minecraft's controls menu.

Client configuration is saved as pretty-printed JSON at `.minecraft/config/badcompany/client.json` in the active run directory. Module enabled states, keybinds, settings, and ClickGUI panel positions are saved through the shared config manager.

## Commands

BadCompany chat commands use the `.` prefix by default. The `FFP Commands` module can disable the client command handler or change the single-character prefix:

| Command | Description |
| --- | --- |
| `.help` | Lists available commands. |
| `.modules` | Lists registered modules. |
| `.toggle <module>` | Toggles a module by display name. |
| `.bind <module> <key.translation.id>` | Binds a module to a Minecraft key translation id, such as `key.keyboard.g`. |
| `.config save` | Saves the current configuration immediately. |

## Current Module List

| Module | Category | Status | Notes |
| --- | --- | --- | --- |
| Advanced Search | Render | Fully ported | Scans loaded client blocks with configurable block/state filters and caches matches for render integrations. |
| Book Formatting | Player | Fully ported | Prints a local formatting-code reminder when the modern book edit screen opens. |
| Bowbomb | Exploit | Fully ported safe equivalent | Detects normal charged bow releases and never sends packet-flood behavior. |
| Entity Trace | Render | Fully ported | Reports large entity position jumps from the loaded client entity cache. |
| FFP Commands | Misc | Fully ported | Restores the legacy command enable switch and configurable prefix. |
| Ignore Players | Misc | Fully ported | Hides chat from configured player names with an optional game-message filter. |
| Packet Canceler | Exploit | Fully ported safe equivalent | Saves packet filter notes without dropping protocol traffic. |
| Pig POV | Render | Fully ported | Switches to first person while riding pigs and restores the previous perspective afterward. |
| Portal Invulnerability | Exploit | Fully ported safe equivalent | Reports portal contact and dimension changes while preserving vanilla teleport handling. |
| PumpkinAura | World | Fully ported | Places held pumpkins with normal vanilla interactions on legal supports and with configurable safety gates. |
| Silent Close | Exploit | Fully ported safe equivalent | Tracks handled-screen close transitions without suppressing vanilla close packets. |
| Stalker | Player | Fully ported | Watches tab-list joins, leaves, and game-mode changes using modern client state. |
| True Durability | Render | Fully ported | Adds exact durability and unbreakable markers to modern item tooltips. |
| Undead | Player | Fully ported | Dismisses the client death screen without sending automatic respawn packets. |

## ClickGUI

The ClickGUI opens with the backslash (`\`) keybind by default, renders module panels above the configured background layer, and avoids the heavy vanilla fullscreen blur. The background selector cycles through `None`, `Light Dim`, `Dark Dim`, and `Blur`; the default is `Light Dim`.

## GitHub Actions

The repository is expected to stay green on these workflows:

- **Build**: runs `./gradlew build --stacktrace` and uploads `build/libs/*.jar`.
- **Java CI with Gradle**: runs the Java 21 Gradle build as a second CI entrypoint.
- **runClient Smoke Test**: launches `./gradlew --no-daemon runClient` under `xvfb` and waits for the BadCompany client initializer log line.

## Migration Status

The Minecraft `1.21.11+` Fabric migration is complete. The production path now consists of a Java 21 Fabric Loom build, a Fabric client initializer, a module registry, ClickGUI, command manager, and JSON configuration persistence. Exploit-heavy packet behavior from the historical client has been replaced with safe client-side monitoring or configuration-only modules where direct behavior would be unsafe or inappropriate on modern servers.

See [`MIGRATION_SUMMARY.md`](MIGRATION_SUMMARY.md) for the final migration summary, architecture notes, module status table, and known modern-Fabric limitations.
