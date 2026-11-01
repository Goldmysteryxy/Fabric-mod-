package dev.terminal.module;

import dev.terminal.core.ModuleManager;

/** ADD A NEW MODULE: write a class extending Module, then add one line here. */
public final class Modules {
    public static void registerAll() {
        // Player
        ModuleManager.register(new NoFall());
        ModuleManager.register(new AntiHunger());
        ModuleManager.register(new AutoRespawn());
        ModuleManager.register(new ToggleSprint());
        ModuleManager.register(new Flight());
        ModuleManager.register(new Speed());
        ModuleManager.register(new Step());
        ModuleManager.register(new AutoRestock());
        ModuleManager.register(new AutoEat());
        ModuleManager.register(new AutoTool());
        ModuleManager.register(new AutoArmor());
        ModuleManager.register(new AutoTotem());
        ModuleManager.register(new AutoFish());
        ModuleManager.register(new AntiAFK());
        ModuleManager.register(new AutoWalk());
        ModuleManager.register(new SafeWalk());
        ModuleManager.register(new Spider());
        ModuleManager.register(new Jesus());
        ModuleManager.register(new Reach());
        ModuleManager.register(new Scaffold());
        ModuleManager.register(new InvTweaks());
        ModuleManager.register(new HoldKeys());
        ModuleManager.register(new FastPlace());
        ModuleManager.register(new ToggleSneak());
        ModuleManager.register(new ToolGuard());
        // Render
        ModuleManager.register(new Esp());
        ModuleManager.register(new Fullbright());
        ModuleManager.register(new Zoom());
        ModuleManager.register(new Nametags());
        ModuleManager.register(new VisualTweaks());
        // World
        ModuleManager.register(new OreFinder());
        ModuleManager.register(new BlockEsp());
        ModuleManager.register(new Waypoints());
        ModuleManager.register(new Nuker());
        ModuleManager.register(new Automation());
        ModuleManager.register(new LightLevel());
        // HUD
        ModuleManager.register(new PotionHud());
        ModuleManager.register(new InfoHud());
        ModuleManager.register(new Tracers());
        ModuleManager.register(new Radar());
        ModuleManager.register(new TargetHud());
        ModuleManager.register(new Keystrokes());
        ModuleManager.register(new ArmorHud());
        ModuleManager.register(new InfoLines());
        ModuleManager.register(new PlayerStats());
        ModuleManager.register(new MaceHud());
        ModuleManager.register(new Crosshair());
        ModuleManager.register(new ModuleList());
        ModuleManager.register(new Notifications());
        // Misc
        ModuleManager.register(new ChatTweaks());
        ModuleManager.register(new Friends());
        ModuleManager.register(new GuiTheme());
    }
}
