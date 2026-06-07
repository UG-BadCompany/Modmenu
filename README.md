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

The first Fabric milestone focuses on a stable boot path, a configurable keybind, a modern ClickGUI shell, JSON configuration, chat command dispatch, and safe module registration. Legacy features that require deeper 1.21 packet/rendering hooks are represented as placeholders so the project remains compilable while preserving the feature map for future work.
