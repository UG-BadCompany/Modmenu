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

Status labels are intentionally conservative: modules are not marked working unless they have a visible modern client behavior beyond toggling. Unsafe legacy packet/exploit behavior remains disabled instead of being represented as a successful port.

| Module | Category | Status | Notes |
| --- | --- | --- | --- |
| Advanced Search | Render | Partial | Incrementally scans nearby loaded blocks for configured ids/state filters and emits client-side highlight particles; full boxed/tracer world rendering is still incomplete. |
| Book Formatting | Player | Partial | Detects modern book editing screens and shows formatting-code help; direct legacy text injection is not restored yet. |
| Bowbomb | Exploit | Unsafe/Disabled | The legacy packet-burst behavior is deliberately disabled on modern Minecraft; the GUI prevents enabling it. |
| Entity Trace | Render | Partial | Tracks loaded entities and reports large position jumps; full legacy tracer rendering still needs a dedicated world-render hook. |
| FFP Commands | Misc | Working | Restores the legacy command enable switch and configurable prefix. |
| Ignore Players | Misc | Working | Hides chat from configured player names with an optional game-message filter. |
| Packet Canceler | Exploit | Partial | Exposes safe clientbound/serverbound filter lists and audit notices; it does not silently drop modern protocol traffic. |
| Pig POV | Render | Working | Switches to first person while riding pigs and restores the previous perspective afterward. |
| Portal Invulnerability | Exploit | Unsafe / Not portable | Detects portal contact for local notices only; legacy teleport-confirm suppression is not safe or portable on modern servers. |
| PumpkinAura | World | Partial | Uses normal vanilla interaction to place held pumpkins on legal supports with safety gates; exploit-style automation is not claimed. |
| Silent Close | Exploit | Partial | Records handled-screen close transitions and last container details; close-packet suppression is not claimed in the port. |
| Stalker | Player | Working | Watches tab-list joins, leaves, names, and game-mode changes using modern client state. |
| True Durability | Render | Working | Adds exact durability and unbreakable markers to modern item tooltips. |
| Undead | Player | Partial | Dismisses the client death screen locally without sending automatic respawn packets. |
| Hunting intelligence modules | Hunting | Partial | The database/scanning pipeline is functional, but overlay-heavy modules remain partial until visual render integrations are complete. |


### Hunting Intelligence Module Status

| Module | Category | Status | Notes |
| --- | --- | --- | --- |
| AI Base Predictor | Hunting | Partial | Correlates local database records into predictions; needs more validation before being called working. |
| Artificial Block Detector | Hunting | Partial | Scans loaded blocks for suspicious artificial materials and records findings. |
| Automatic Evidence Capture | Hunting | Partial | Captures nearby evidence into the local hunt database; no screenshot/media capture is implemented. |
| Base Archaeology | Hunting | Partial | Scans for old-base evidence such as obsidian, utility blocks, and terrain scars. |
| Book Archive | Hunting | Partial | Archives written books found client-side, but not all legacy book workflows are restored. |
| Chunk Change Tracker | Hunting | Partial | Tracks revisited chunk changes in the local database; visual changed-chunk overlay is pending. |
| Elytra Flight Logger | Hunting | Partial | Logs flight path samples and distance statistics client-side. |
| Expedition System | Hunting | Partial | Generates local investigation records from existing evidence; planner UX remains basic. |
| Exploration Statistics | Hunting | Partial | Records exploration counters and autosaves them locally. |
| Hidden Entrance Detector | Hunting | Partial | Scans loaded blocks for likely entrance indicators; overlay rendering is pending. |
| Highway AI | Hunting | Partial | Records highway-like evidence in loaded chunks. |
| Highway Divergence Detector | Hunting | Partial | Detects branch/tunnel evidence near highways in loaded blocks. |
| Hunt Database | Hunting | Working | Provides persistent local JSON storage used by the hunting modules. |
| Investigation Notes | Hunting | Partial | Stores coordinate notes locally; in-game editing UX is minimal. |
| Long Distance ESP | Hunting | Partial | Collects targets in loaded range; long-distance visual ESP overlay remains partial. |
| Multi-Session Intelligence Engine | Hunting | Partial | Combines persisted evidence across sessions; predictions need further validation. |
| Player Activity Heatmap | Hunting | Partial | Records loaded player sightings for heatmap data; heatmap overlay is pending. |
| Portal Database | Hunting | Partial | Records loaded nether portals and timestamps in the hunt database. |
| Region Scanner | Hunting | Partial | Scores visited regions from database evidence. |
| Safe Logout Finder | Hunting | Partial | Scores nearby terrain for safer logout candidates; recommendations need validation. |
| Search Planner | Hunting | Partial | Suggests search areas from existing evidence; no advanced route UI yet. |
| Shared Hunt Files | Hunting | Partial | Provides safe import/export-oriented database integration. |
| Sign Database | Hunting | Partial | Saves visible sign locations/text where the client exposes them. |
| Stash Probability Scanner | Hunting | Partial | Scores storage/utility clusters in loaded blocks. |
| Terrain Anomaly Finder | Hunting | Partial | Scans loaded terrain for suspicious shapes; visual overlay is pending. |
| Torch Cluster Finder | Hunting | Partial | Detects suspicious torch clusters in loaded blocks. |
| Trail Mapper | Hunting | Partial | Records artificial trail evidence over sessions; tracer rendering is pending. |
| World Heatmap | Hunting | Partial | Builds heatmap-ready data layers; visual overlay is pending. |
| World History Viewer | Hunting | Partial | Summarizes local historical database records; dedicated viewer UX remains basic. |

## ClickGUI

The ClickGUI opens with the backslash (`\`) keybind by default and now follows the compact legacy `1.12.2` Family Fun Pack layout: black translucent panels, one-pixel accent borders, small category headers, compact module rows, and non-stretched text. The top controls expose the new GUI settings: UI scale, accent color, panel width, compact/roomy mode, layout reset, background mode, and the intelligence dashboard. Module rows use compact legacy labels plus small on/off switches; unsafe modules render with warning coloring and refuse to enable, so placeholder or unsafe modules are not presented as complete ports.

## GitHub Actions

The repository is expected to stay green on these workflows:

- **Build**: runs `./gradlew build --stacktrace` and uploads `build/libs/*.jar`.
- **Java CI with Gradle**: runs the Java 21 Gradle build as a second CI entrypoint.
- **runClient Smoke Test**: launches `./gradlew --no-daemon runClient` under `xvfb` and waits for the BadCompany client initializer log line.

## Migration Status

The Minecraft `1.21.11+` Fabric migration is not considered complete until every listed module is either verified working in-game or documented as partial/unsafe with a concrete technical blocker. The production path now consists of a Java 21 Fabric Loom build, a Fabric client initializer, a module registry, ClickGUI, command manager, and JSON configuration persistence. Exploit-heavy packet behavior from the historical client has been replaced with safe client-side monitoring or configuration-only modules where direct behavior would be unsafe or inappropriate on modern servers.

See [`MIGRATION_SUMMARY.md`](MIGRATION_SUMMARY.md) for the final migration summary, architecture notes, module status table, and known modern-Fabric limitations.
