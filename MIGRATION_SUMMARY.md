# Final Fabric Migration Summary

## Migration Completed

BadCompany targets modern Minecraft 1.21.x on Fabric Loader `0.19.3` with Java 21. The active code path is a Fabric client mod built with Fabric Loom and Yarn mappings. Historical Forge/MCP sources are retained only under `legacy/` for reference and are excluded from compilation.

## Legacy Systems Removed From Production

- Forge mod entrypoints and event bus wiring.
- MCP mapping names and ForgeGradle build logic.
- Unsafe packet-flood behavior from exploit modules.
- Direct close-packet or teleport-packet suppression without a dedicated audited Fabric packet hook.

## Current Architecture

- `BadCompanyClient` initializes keybindings, module registration, config loading, command handling, lifecycle saving, and per-tick dispatch.
- `ModuleManager` owns module registration, category lookup, name lookup, ticking, keybind toggles, and save requests.
- `Module` provides the shared enabled/keybind/settings lifecycle used by every feature module.
- `ConfigManager` persists the GUI keybind, ClickGUI panel state, GUI background mode, module enabled state, module keybinds, and typed setting values as JSON.
- `ClickGuiScreen`, `CategoryPanel`, and `ModuleButton` provide the client GUI for browsing, toggling, and editing registered modules.
- `CommandManager` provides the active client-side chat command set when the `FFP Commands` module is enabled.

## Module Parity Status

See [`modules.md`](modules.md) for the complete legacy-to-Fabric audit. The original module registry contains fourteen user-facing modules, and all fourteen now have Fabric implementations:

| Module | Result |
| --- | --- |
| Advanced Search | Fully ported loaded-block scanning and cached-match model. |
| Book Formatting | Fully ported book formatting reminders for modern book editing screens. |
| Bowbomb | Fully ported safe charged-bow monitor with unsafe packet flooding disabled. |
| Entity Trace | Fully ported entity jump tracing against the client entity cache. |
| FFP Commands | Fully ported command enable switch and configurable prefix. |
| Ignore Players | Fully ported client-side chat ignore list. |
| Packet Canceler | Fully ported safe packet filter configuration and audit state. |
| Pig POV | Fully ported pig-riding perspective helper. |
| Portal Invulnerability | Fully ported portal and dimension-transition monitor with modern safety limits. |
| PumpkinAura | Fully ported vanilla pumpkin placement helper. |
| Silent Close | Fully ported handled-screen close tracker with modern safety limits. |
| Stalker | Fully ported tab-list join, leave, and game-mode tracking. |
| True Durability | Fully ported exact durability and unbreakable tooltip markers. |
| Undead | Fully ported client-side death-screen dismissal. |

## ClickGUI Usability Pass

- The ClickGUI overrides vanilla screen blur so opening the menu no longer creates a heavy blurred pause-menu effect.
- Panels and module rows render over the configured background layer with opaque, high-contrast colors.
- Text uses shadowed white or near-white colors for readability.
- A persisted background selector cycles through `None`, `Light Dim`, `Dark Dim`, and `Blur`; `Light Dim` is the default.

## Known Modern-Fabric Limitations

Some legacy exploit behaviors relied on Forge/MCP-era packet interception or packet flooding. The Fabric port preserves the module surface area, settings, state persistence, and closest safe equivalent behavior, but does not silently drop or flood live protocol packets without a future audited networking hook.

## Validation Targets

Before merge, validate the repository with:

```bash
./gradlew build
./gradlew runClient
```

The Build, Java CI with Gradle, and runClient Smoke Test GitHub Actions workflows should also pass on pull requests.
