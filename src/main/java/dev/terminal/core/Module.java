package dev.terminal.core;

import dev.terminal.gui.Gfx;
import dev.terminal.setting.Setting;
import java.util.ArrayList;
import java.util.List;

/** Extend this, add settings with add(...), override the hooks you need, register in Modules.java. */
public abstract class Module {
    public final String name, description;
    public final Category category;
    public final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;
    private int keyCode = -1;

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    protected <S extends Setting<?>> S add(S setting) { settings.add(setting); return setting; }

    public String id() { return name.toLowerCase().replace(" ", ""); }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean on) {
        if (on == enabled) return;
        enabled = on;
        if (on) onEnable(); else onDisable();
        dev.terminal.module.Notifications.push(name, on);
    }
    public void toggle() { setEnabled(!enabled); }
    public int keyCode() { return keyCode; }
    public void setKeyCode(int k) { keyCode = k; }

    public Setting<?> find(String key) {
        for (Setting<?> s : settings) if (s.key().equals(key)) return s;
        return null;
    }

    // ---- hooks ----
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onHud(Gfx g) {}
    /** Called when the game closes (restore anything you changed in vanilla options). */
    public void onShutdown() {}
}
