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

The Fabric 1.21.x migration is stable: the project has a Java 21 Gradle/Fabric Loom build path, GitHub Actions build coverage, a runClient smoke workflow, a module framework, JSON config, and ClickGUI. True Durability and Advanced Search are active ports. Book Formatting, Entity Trace, Stalker, and Undead now use safe 1.21.11 client APIs for local notices, tab-list tracking, entity jump tracking, and death-screen handling. Exploit/network-sensitive modules remain safe placeholders until a direct modern API migration is available.

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
- [x] Book Formatting
- [ ] Silent Close
- [ ] Packet Canceler
- [ ] Portal Invulnerability
- [ ] Pig POV
- [x] Entity Trace
- [x] Stalker
- [x] Undead
- [ ] Bowbomb
- [ ] PumpkinAura
