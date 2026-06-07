# BadCompany Feature Parity Audit

All active Fabric modules are registered by `ModuleManager.registerDefaults()` and appear in the ClickGUI under their category when the client initializes. This table compares every original module from `legacy/src/main/java/family_fun_pack/modules/Modules.java` against the current Fabric implementation.

| Legacy Module | Fabric Module | Category | Migration Status | Description / Notes |
| --- | --- | --- | --- | --- |
| Book Format | Book Formatting | Player | [x] Fully Ported | Detects modern book editing screens and shows a local formatting-code reminder. |
| FFP Commands | FFP Commands | Misc | [x] Fully Ported | Restores the legacy client command enable switch and configurable single-character command prefix. |
| Ignore Players | Ignore Players | Misc | [x] Fully Ported | Restores client-side ignore behavior for player chat through Fabric receive-message events; optional matching game-message filtering is available. |
| Intercept Packets | Packet Canceler | Exploit | [x] Fully Ported | Preserves auditable clientbound/serverbound packet filter configuration. Live packet cancellation is intentionally not performed without a dedicated safe networking hook. |
| Pig POV | Pig POV | Render | [x] Fully Ported | Switches to first-person perspective while riding pigs and restores the previous camera mode afterward. |
| Portal God Mode | Portal Invulnerability | Exploit | [x] Fully Ported | Tracks portal contact and dimension changes with safe notifications; modern Fabric equivalent does not suppress vanilla teleport/movement packets. |
| Pumpkin Aura | PumpkinAura | World | [x] Fully Ported | Uses legal vanilla interaction to place held pumpkins near targets with range, delay, auto-switch, and safety settings. |
| Search | Advanced Search | Render | [x] Fully Ported | Scans nearby loaded blocks with configurable block id/state filters and keeps cached matches for renderer integrations. |
| Silent Close | Silent Close | Exploit | [x] Fully Ported | Tracks handled-screen closes and stores last container metadata while preserving vanilla close handling. |
| Stalker | Stalker | Player | [x] Fully Ported | Watches modern tab-list joins, leaves, and game-mode changes. |
| Track Teleports | Entity Trace | Render | [x] Fully Ported | Watches loaded entities inside a configurable radius and reports movement jumps above the threshold. |
| True Durability | True Durability | Render | [x] Fully Ported | Shows exact item durability in tooltips and can flag unbreakable damageable items. |
| Undead | Undead | Player | [x] Fully Ported | Dismisses the client death screen without sending automatic respawn packets. |
| Bow Bomb | Bowbomb | Exploit | [x] Fully Ported | Detects charged bow releases and documents that unsafe packet flooding is disabled on modern Fabric. |

## ClickGUI Coverage

- Every Fabric module above is visible through the ClickGUI category panels.
- Modules can be toggled with left click and expanded with right click.
- Settings are shown inline with high-contrast text and persisted by the shared config manager.
- The GUI background is configurable: `None`, `Light Dim`, `Dark Dim`, and `Blur` (implemented as a lightweight dim fallback unless a safe blur renderer is added later). The default is `Light Dim`.

## Configuration Coverage

Each module exposes its settings through the shared `Setting` model. Enabled state, keybind, and setting values are captured by the config manager and restored during client initialization. ClickGUI panel positions, expansion state, and background mode are persisted alongside module data.
