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

The Fabric 1.21.x migration is stable: the project has a working Java 21 Gradle/Fabric Loom build path, GitHub Actions build coverage, a runClient smoke workflow, a module framework, JSON config, ClickGUI, and the True Durability port. Advanced Search now has a safe client-side block/state search cache; its settings are persisted in JSON, while deeper box/tracer render pipeline polish remains a follow-up. Legacy features that still require deeper 1.21 packet/rendering hooks remain placeholders until each module is safely ported in its own PR.

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

- [x] True Durability
- [x] Advanced Search
- [ ] Book Formatting
- [ ] Silent Close
- [ ] Packet Canceler
- [ ] Portal Invulnerability
- [ ] Pig POV
- [ ] Entity Trace
- [ ] Stalker
- [ ] Undead
- [ ] Bowbomb
- [ ] PumpkinAura
