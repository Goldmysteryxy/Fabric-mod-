package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;

/** Lowers the FOV option while enabled. Bind it to a key and tap to zoom. */
public class Zoom extends Module {
    private final NumberSetting fov = add(new NumberSetting("Zoom FOV", "FOV while zoomed (30 = max zoom)", 30, 30, 90, 1));
    private int original = -1;

    public Zoom() { super("Zoom", "Zoom in", Category.RENDER); }

    @Override public void onTick() {
        var opt = Minecraft.getInstance().options.fov();
        if (original < 0) original = opt.get();
        if (opt.get() != fov.intValue()) opt.set(fov.intValue());
    }
    @Override public void onDisable() { restore(); }
    @Override public void onShutdown() { restore(); }

    private void restore() {
        if (original >= 0) { Minecraft.getInstance().options.fov().set(original); original = -1; }
    }
}
