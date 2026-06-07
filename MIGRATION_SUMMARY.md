# Final Fabric Migration Summary

## Migration Completed

BadCompany now targets Minecraft `1.21.11` on Fabric Loader `0.19.3` with Java 21. The active code path is a Fabric client mod built with Fabric Loom and Yarn mappings. Historical Forge/MCP sources are retained only under `legacy/` for reference and are excluded from compilation.

## Legacy Systems Removed From Production

- Forge mod entrypoints and event bus wiring.
- MCP mapping names and ForgeGradle build logic.
- Direct packet-cancel and packet-flood behaviors from exploit modules.
- Historical GUI/command documentation for commands that are not registered in the Fabric client.
- Compatibility comments and temporary task markers from the active Java sources.

## Current Architecture

- `BadCompanyClient` initializes keybindings, module registration, config loading, command handling, lifecycle saving, and per-tick dispatch.
- `ModuleManager` owns module registration, category lookup, name lookup, ticking, keybind toggles, and save requests.
- `Module` provides the shared enabled/keybind/settings lifecycle used by every feature module.
- `ConfigManager` persists the GUI keybind, ClickGUI panel state, module enabled state, module keybinds, and typed setting values as JSON.
- `ClickGuiScreen`, `CategoryPanel`, and `ModuleButton` provide the client GUI for browsing, toggling, and editing registered modules.
- `CommandManager` provides the active client-side chat command set.

## Modules Ported

| Module | Result |
| --- | --- |
| Advanced Search | Partial safe port: configurable loaded-block scanning and cached matches are implemented. |
| Book Formatting | Safe port: formatting reminders are implemented for modern book editing screens. |
| Bowbomb | Safe monitor: bow release detection is implemented without unsafe packet flooding. |
| Entity Trace | Safe port: entity jump tracing is implemented against the client entity cache. |
| Packet Canceler | Safe configuration module: packet filter persistence is implemented without live packet dropping. |
| Pig POV | Safe port: pig-riding perspective helper is implemented. |
| Portal Invulnerability | Safe monitor: portal and dimension-transition notices are implemented without teleport suppression. |
| PumpkinAura | Partial safe port: vanilla pumpkin placement helper is implemented with range, delay, auto-switch, and safety settings. |
| Silent Close | Safe monitor: handled-screen close tracking is implemented without close-packet suppression. |
| Stalker | Safe port: tab-list join, leave, and game-mode tracking is implemented. |
| True Durability | Safe port: exact durability and unbreakable tooltip markers are implemented. |
| Undead | Safe port: client-side death-screen dismissal is implemented without automatic respawn packets. |

## Remaining Placeholders

- Advanced Search and PumpkinAura expose cached targets/colors for render integrations, but dedicated world overlay rendering is not part of this final cleanup pass.
- Packet Canceler intentionally remains configuration-only unless an audited, safe packet hook is designed later.
- Exploit-named modules keep their familiar names for user continuity, but unsafe packet manipulation has been removed from production behavior.

## Validation Targets

Before merge, validate the repository with:

```bash
./gradlew build
./gradlew runClient
```

The Build, Java CI with Gradle, and runClient Smoke Test GitHub Actions workflows should also pass on pull requests.
