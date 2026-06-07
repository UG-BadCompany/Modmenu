package com.badcompany.modmenu.module;

/**
 * Port audit for the legacy 1.12.2 modules that are exposed in the modern GUI.
 *
 * <table>
 *   <caption>BadCompany legacy-module port status</caption>
 *   <tr><th>Module</th><th>Status</th><th>Technical reason</th></tr>
 *   <tr><td>True Durability</td><td>Working</td><td>Fabric tooltip callback displays exact remaining durability and unbreakable markers.</td></tr>
 *   <tr><td>Advanced Search</td><td>Partial</td><td>Client block scanner works and emits local highlight particles; boxed/tracer world rendering still needs a dedicated render backend before this can be called fully ported.</td></tr>
 *   <tr><td>Book Formatting</td><td>Partial</td><td>Modern book screens accept formatting text, but the legacy formatting insertion GUI has not been fully recreated.</td></tr>
 *   <tr><td>Silent Close</td><td>Partial</td><td>Modern handled-screen close packets are server-validated; this module only tracks close state unless a safe packet hook is added.</td></tr>
 *   <tr><td>Packet Canceler</td><td>Partial</td><td>Filter configuration exists; packet dropping is intentionally blocked until explicit allow-listed interception is implemented.</td></tr>
 *   <tr><td>Portal Invulnerability</td><td>Unsafe / Not portable</td><td>The legacy exploit depended on withholding portal/teleport confirmations and is unsafe on modern servers.</td></tr>
 *   <tr><td>Pig POV</td><td>Working</td><td>Tick hook detects pig riding and switches/restores the vanilla camera perspective.</td></tr>
 *   <tr><td>Entity Trace</td><td>Partial</td><td>Entity movement tracking works; visual trace/highlight rendering remains incomplete.</td></tr>
 *   <tr><td>Stalker</td><td>Working</td><td>Modern tab-list polling reports joins, leaves, and game-mode changes.</td></tr>
 *   <tr><td>Undead</td><td>Partial</td><td>Client death-screen dismissal is available; automatic respawn/exploit behavior is intentionally not sent.</td></tr>
 *   <tr><td>Bowbomb</td><td>Unsafe / Not portable</td><td>The legacy packet burst is an exploit and is disabled instead of faked.</td></tr>
 *   <tr><td>PumpkinAura</td><td>Partial</td><td>Uses normal vanilla pumpkin placement only; legacy automation/exploit behavior is not reproduced.</td></tr>
 * </table>
 */
public final class ModuleAudit {
    private ModuleAudit() {}
}
