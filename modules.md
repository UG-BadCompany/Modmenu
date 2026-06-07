# BadCompany Modules

All modules are registered by `ModuleManager.registerDefaults()` and appear in the ClickGUI under their category when the client initializes.

| Module | Category | Status | Description |
| --- | --- | --- | --- |
| True Durability | Render | Safe port | Shows exact item durability in tooltips and can flag unbreakable damageable items. |
| Advanced Search | Render | Partial safe port | Scans nearby loaded blocks against block id and block-state filters, caching matches for renderer integrations. |
| Book Formatting | Player | Safe port | Shows a local formatting-code reminder when a book editing screen opens. |
| Silent Close | Exploit | Safe monitor | Tracks handled-screen closes and remembers the last container title/sync id while preserving vanilla close handling. |
| Packet Canceler | Exploit | Safe configuration module | Saves clientbound/serverbound packet filter lists for audits without dropping live protocol traffic. |
| Portal Invulnerability | Exploit | Safe monitor | Reports portal contact and dimension changes without withholding vanilla teleport or movement packets. |
| Pig POV | Render | Safe port | Switches to first-person perspective while riding pigs and restores the previous camera mode afterward. |
| Entity Trace | Render | Safe port | Watches loaded entities inside a configurable radius and reports movement jumps above the configured threshold. |
| Stalker | Player | Safe port | Watches tab-list joins, leaves, and game-mode changes through the modern player list API. |
| Undead | Player | Safe port | Dismisses the client death screen without sending automatic respawn packets. |
| Bowbomb | Exploit | Safe monitor | Detects normal charged bow releases and reports that packet flooding is disabled. |
| PumpkinAura | World | Partial safe port | Uses normal vanilla block interaction to place held pumpkins on legal supports near player targets with configurable safety gates. |

## Configuration Coverage

Each module exposes its settings through the shared `Setting` model. Enabled state, keybind, and setting values are captured by the config manager and restored during client initialization. ClickGUI panel positions and expansion state are persisted alongside module data.
