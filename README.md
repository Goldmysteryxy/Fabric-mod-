# Terminal (Fabric 26.3, client-only)

Open the GUI with **Right Shift** (change with `guikey <key>` in the terminal).

GUI: left-click module = toggle, right-click = settings, middle-click = bind key.
Number settings: click the bar to set, right-click resets. Scroll over a panel to scroll it.
Terminal (bottom bar): `help`, `list`, `toggle esp`, `set esp range 128`, `bind fullbright g`,
`get oreFinder`, `profile save pvp`, `profile load pvp`.

## Automation and Tweakeroo-style tweaks
`Automation` (World panel) farms and replants crops, mines blocks and hits mobs inside your reach, in the priority order you pick.
Farm is on by default; Mine and Hit are off. Optional **Seek** walks in a straight line toward the nearest target (experimental,
no pathfinding). It pauses at low health and shows a status line you can drag with `hudedit`.
Also: `HoldKeys`, `FastPlace`, `ToggleSneak`, `ToolGuard`, `InfoLines` (biome, light, speed, ping, looking-at...), `VisualTweaks`.

## Customizing the GUI
Open **GuiTheme** (Misc panel): presets (Dark, Midnight, Forest, Sunset, Light, Custom), accent/header colors, opacity,
panel width, row height, tooltips, terminal log and background dimming. Drag a panel header to move it; right-click a header
to collapse it. `guireset` puts everything back. Layout is saved in the config and in profiles.

## Player stats
`PlayerStats` lists nearby players with health bar, armor, held item, distance and status. `TargetHud` now also shows armor,
held item and distance for whatever you're looking at.

## Skins (main menu)
The title screen has a **Skins** button. Drag .png files (64x64 or 64x32) onto that screen to add them (limit 8 by default,
change with the `skin max <n>` terminal command). Set classic/slim per skin, then **Apply** to upload it to your account
through Mojang's official skin API. Rejoin the world (or restart) to see it. Needs a real Microsoft login and is rate limited.
Files live in `config/terminalmod/skins`.

## HUD editor
`hudedit` opens a screen where you drag enabled HUD elements (anything with X/Y settings) around. Esc to finish.

## Armor and mace HUDs
`ArmorHud` lists armor (and optionally held items) with durability, a red warning below a set %, and enchantments.
`MaceHud` shows fall distance, smash-ready, estimated damage now and at the ground, attack charge, wind charges and mace enchants.
Both are draggable with `hudedit`.

## Macros
`macro add <name> <cmd; cmd; ...>`, `macro bind <name> <key>`, `macro run <name>`, `macro show <name>`,
`macro repeat <name> <ticks|off>`, `macro del <name>`, `macro list`. Use `wait 20` (ticks) between steps.
Example: `macro add mining on fullbright; on oreFinder; set blockesp blocks diamond_ore; wait 100; off fullbright`
The GUI has a Macros panel: left click runs, right click toggles repeat, middle click binds a key.
Macros are saved in the config and in profiles.

## Adding a module
1. Create a class extending `Module` in `dev.terminal.module`, add settings with `add(...)`.
2. Override `onTick`, `onHud`, `onEnable`, `onDisable` as needed.
3. Add one line to `Modules.registerAll()`. GUI, config, keybinds and terminal support are automatic.

## Version-sensitive spots (edit these if a Minecraft update breaks the build)
- `gui/Gfx.java` (draw calls), `util/Mc.java` (key polling, server player)
- `gui/ClickGuiScreen.java` (Screen/input method signatures)
- `module/Targets.java`, `Tracers.java` (entity package names)
- `TerminalMod` title-screen button (`Screens.getButtons`), `util/Mc.java#openScreen` (setScreenAndShow), `openFolder`
- `util/Mc.java` wrappers: inventory click, hotbar select (`setSelectedSlot`), `useItemOn`, `useItem`, destroy-block calls, `player.fishing`
- `mixin/MinecraftMixin.java` (method name `shouldEntityAppearGlowing`)

NoFall / AntiHunger work through the integrated server, so they only do anything in singleplayer.

BlockEsp and Waypoints are drawn with screen-space projection (`util/Projector.java`), not world rendering.
Modules can register their own terminal commands (see `Waypoints`).

## Building the jar on GitHub
1. Create a repository and upload the *contents* of this folder (so `build.gradle` and `.github/` sit at the repo root).
2. Push. The **Build** workflow runs automatically (or open the Actions tab and run it manually).
3. When it finishes, open the run and download the `terminalmod-jar` artifact. The mod jar is inside.
4. To publish a Release, push a tag like `v1.0.0`; the jar is attached automatically.

The workflow installs Java 25 and Gradle 9.4.1 itself, so no wrapper files are needed. To build locally, install Gradle 9.4.1
and run `gradle build`; the jar appears in `build/libs/`. If the template at fabricmc.net/develop recommends a newer Gradle or
Loom, change `gradle-version` in `.github/workflows/build.yml` and the Loom version in `build.gradle`.
