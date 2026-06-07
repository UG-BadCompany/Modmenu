# BadCompany Modmenu

BadCompany is a modern Fabric client rebuild of the legacy 1.12.2 Forge utility client. The old Forge/MCP implementation has been moved to `legacy/` for reference only and is excluded from compilation.

## Target Platform

- Minecraft Java `1.21.11`
- Fabric Loader `0.19.3`
- Fabric API `0.141.4+1.21.11`
- Fabric Loom `1.14.8`
- Java `21`
- Yarn mappings `1.21.11+build.6`

## Development

```bash
./gradlew build
./gradlew runClient
```

The default GUI keybind is backslash (`\\`) and can be changed from Minecraft's keybind settings. Client configuration is stored as JSON under `.minecraft/config/badcompany/client.json`.

## Current Migration Status

The Fabric 1.21.x migration is stable: the project has a Java 21 Gradle/Fabric Loom build path, GitHub Actions build coverage, a runClient smoke workflow, a module framework, JSON config, and ClickGUI. True Durability, Advanced Search, Book Formatting, Entity Trace, Stalker, Undead, Pig POV, and PumpkinAura are active or safe-behavior ports using 1.21.11 client APIs. Silent Close, Packet Canceler, Portal Invulnerability, and Bowbomb are safely stubbed/monitor-only where the original packet exploit behavior is no longer valid for modern Fabric without unsafe packet suppression.

## Migration Checklist

### Core Framework

- [x] Fabric 1.21.x conversion
- [x] Java 21
- [x] Gradle/Fabric Loom build
- [x] GitHub Actions build workflow
- [x] runClient smoke workflow
- [x] Module system
- [x] Config system
- [x] ClickGUI
- [x] True Durability

### Legacy Modules

Legend: **Fully ported** means the Fabric module provides the original safe client-side behavior. **Partially ported** means a modern equivalent exists but rendering/mixin expansion is still pending. **Safely stubbed** means exploit-heavy packet behavior is intentionally not reproduced; settings and monitor-mode behavior are saved through the modern config system.

| Module | Status | Notes |
| --- | --- | --- |
| True Durability | Fully ported | Exact durability tooltip values use modern item components. |
| Advanced Search | Partially ported | Block/state filtering and cached target scanning are active; dedicated render overlays remain pending. |
| Book Formatting | Fully ported | Modern book-edit screen notices preserve safe formatting guidance. |
| Silent Close | Safely stubbed | Tracks handled-screen closes and last container details without suppressing vanilla close packets. |
| Packet Canceler | Safely stubbed | Saves packet filter lists in safe mode without dropping protocol traffic. |
| Portal Invulnerability | Safely stubbed | Monitors portal contact and dimension transitions without withholding teleport confirmations. |
| Pig POV | Fully ported | Pig riding switches to a safe first-person camera helper without changing entity dimensions. |
| Entity Trace | Fully ported | Reports large entity movement jumps from the loaded client entity cache. |
| Stalker | Fully ported | Tracks tab-list joins/leaves and gamemode changes through the modern player list API. |
| Undead | Fully ported | Safely dismisses the death screen client-side without auto-respawn packets. |
| Bowbomb | Safely stubbed | Detects normal charged bow releases and refuses legacy packet flooding. |
| PumpkinAura | Partially ported | Uses normal vanilla interaction to place held pumpkins on legal supports with configurable safety gates; render overlay remains pending. |
