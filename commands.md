# BadCompany Commands

BadCompany commands are client-side chat commands. Type them into chat with the `.` prefix; handled commands are not sent to the server.

| Command | Usage | Description |
| --- | --- | --- |
| Help | `.help` | Lists all registered BadCompany commands with usage text. |
| Modules | `.modules` | Prints the currently registered module names. |
| Toggle | `.toggle <module>` | Toggles a module by display name. Spaces are supported, for example `.toggle True Durability`. |
| Bind | `.bind <module> <key.translation.id>` | Assigns a module keybind using Minecraft translation ids, for example `.bind Pig POV key.keyboard.g`. |
| Config | `.config save` | Saves module state, keybinds, settings, GUI keybind, and panel positions immediately. |

The default ClickGUI keybind is backslash (`\`). Module keybinds can also be managed through commands and are persisted in `.minecraft/config/badcompany/client.json`.
