### Modules list

##### True Durability
Display items real durability in on-hover tooltip. Can be used to know "unbreakable" items true durability.

Unbreakable items are displayed in red enchant, easier to spot on other players or in chests.

##### Advanced Search
Block search module with advanced options. Blocks highlight + tracers.

Advanced options allow to filter by block states and specify color & tracer options for each search filter.

Example: use it to search for player heads, excluding mobs heads, or spawners for specific mob.

##### Book formatting
Modern safe port: prints a local formatting-code legend when a book editing screen opens. A richer edit-screen panel remains future mixin work.

##### Silent Close
Modern safe stub: tracks handled-screen open/close transitions and remembers the last container title/sync id. It does **not** suppress vanilla close packets on Fabric 1.21.11.

##### Packets Canceling
Modern safe stub: saves clientbound/serverbound packet filter lists for migration continuity, but does **not** drop protocol traffic without an audited packet hook.

##### Portal Invulnerability
Modern safe stub: monitors portal contact and dimension transitions while preserving vanilla teleport confirmation and movement packets.

##### Pig POV
Modern safe port: when riding a pig, optionally switches to first-person perspective without changing entity dimensions or renderers.

##### Trace entities teleporting
Modern safe port: watches loaded client entities inside a configurable radius and prints local chat notices when an entity position jump exceeds the configured teleport threshold.

Example: trace dogs tp.

##### Stalker
Modern safe port: watches the client tab-list cache and prints local chat notices for joins, leaves, and game mode changes. Chat-message matching and command-managed watchlists remain future work.

##### Undead
Modern safe port: can dismiss the client DeathScreen without automatically sending respawn packets, leaving vanilla respawn handling under user control.

##### Bowbomb

Modern safe stub: detects normal charged bow releases and reports that legacy packet flooding is intentionally disabled.

##### PumpkinAura

Modern partial port: uses normal vanilla block interaction to place held pumpkins on legal supports near player targets, with use-key, delay, auto-switch, and bedwars-mode settings. Dedicated render overlay remains pending.
