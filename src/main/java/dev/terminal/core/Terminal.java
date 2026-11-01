package dev.terminal.core;

import dev.terminal.setting.Setting;
import dev.terminal.util.Keys;
import java.util.*;
import java.util.function.Consumer;

/** The command registry + output log shown in the GUI. Add a command with reg(...). */
public final class Terminal {
    public static final List<String> LOG = new ArrayList<>();
    private record Cmd(String usage, Consumer<String[]> run) {}
    private static final Map<String, Cmd> CMDS = new LinkedHashMap<>();

    public static void print(String s) {
        LOG.add(s);
        while (LOG.size() > 60) LOG.remove(0);
    }

    public static void reg(String name, String usage, Consumer<String[]> run) { CMDS.put(name, new Cmd(usage, run)); }

    public static void run(String line) {
        String[] p = line.trim().split("\\s+");
        if (p.length == 0 || p[0].isEmpty()) return;
        Cmd c = CMDS.get(p[0].toLowerCase());
        if (c == null) { print("Unknown command. Type 'help'."); return; }
        try { c.run().accept(Arrays.copyOfRange(p, 1, p.length)); }
        catch (Exception e) { print("Error: " + e); }
    }

    private static Module mod(String q) {
        Module m = ModuleManager.find(q);
        if (m == null) print("No module '" + q + "'. Try 'list'.");
        return m;
    }

    public static void init() {
        reg("help", "help", a -> CMDS.values().forEach(c -> print(c.usage())));
        reg("list", "list - all modules by category", a -> {
            for (Category cat : Category.values()) {
                StringBuilder sb = new StringBuilder(cat.label + ": ");
                boolean any = false;
                for (Module m : ModuleManager.all()) if (m.category == cat) { any = true; sb.append(m.isEnabled() ? "*" : "").append(m.id()).append(' '); }
                if (any) print(sb.toString());
            }
        });
        reg("toggle", "toggle <module>", a -> { if (a.length < 1) { print("toggle <module>"); return; } Module m = mod(a[0]); if (m != null) { m.toggle(); print(m.name + (m.isEnabled() ? " ON" : " off")); } });
        reg("on", "on <module>", a -> { if (a.length < 1) { print("on <module>"); return; } Module m = mod(a[0]); if (m != null) { m.setEnabled(true); print(m.name + " ON"); } });
        reg("off", "off <module>", a -> { if (a.length < 1) { print("off <module>"); return; } Module m = mod(a[0]); if (m != null) { m.setEnabled(false); print(m.name + " off"); } });
        reg("get", "get <module> - show settings", a -> {
            if (a.length < 1) { print("get <module>"); return; }
            Module m = mod(a[0]); if (m == null) return;
            print(m.name + ": " + (m.isEnabled() ? "ON" : "off") + ", key=" + Keys.name(m.keyCode()));
            for (Setting<?> s : m.settings) print("  " + s.key() + " = " + s.display());
        });
        reg("set", "set <module> <setting> <value>", a -> {
            if (a.length < 3) { print("set <module> <setting> <value>"); return; }
            Module m = mod(a[0]); if (m == null) return;
            Setting<?> s = m.find(a[1].toLowerCase());
            if (s == null) { print("No setting '" + a[1] + "'. Try 'get " + m.id() + "'."); return; }
            String v = String.join(" ", Arrays.copyOfRange(a, 2, a.length));
            print(s.parse(v) ? s.key() + " = " + s.display() : "Invalid value '" + v + "'");
        });
        reg("reset", "reset <module> - restore default settings", a -> {
            if (a.length < 1) { print("reset <module>"); return; }
            Module m = mod(a[0]); if (m == null) return;
            m.settings.forEach(Setting::reset); print(m.name + " settings reset");
        });
        reg("bind", "bind <module> <key|none>", a -> {
            if (a.length < 2) { print("bind <module> <key|none>"); return; }
            Module m = mod(a[0]); if (m == null) return;
            int k = Keys.parse(a[1]);
            if (k == Keys.INVALID) { print("Unknown key '" + a[1] + "'"); return; }
            m.setKeyCode(k); print(m.name + " bound to " + Keys.name(k));
        });
        reg("guikey", "guikey <key> - key that opens this GUI", a -> {
            if (a.length < 1) { print("guikey <key>"); return; }
            int k = Keys.parse(a[0]);
            if (k == Keys.INVALID || k < 0) { print("Unknown key"); return; }
            ModuleManager.guiKey = k; print("GUI key = " + Keys.name(k));
        });
        reg("profile", "profile <save|load|delete|list> [name]", a -> {
            if (a.length < 1) { print("profile <save|load|delete|list> [name]"); return; }
            switch (a[0].toLowerCase()) {
                case "list" -> print("Profiles: " + ConfigManager.listProfiles());
                case "save", "load", "delete" -> {
                    if (a.length < 2 || !ConfigManager.validName(a[1])) { print("Give a name (letters, digits, - _)"); return; }
                    boolean ok = switch (a[0].toLowerCase()) {
                        case "save" -> ConfigManager.saveProfile(a[1]);
                        case "load" -> ConfigManager.loadProfile(a[1]);
                        default -> ConfigManager.deleteProfile(a[1]);
                    };
                    print(ok ? "Done: " + a[0] + " " + a[1] : "Failed (does the profile exist?)");
                }
                default -> print("profile <save|load|delete|list> [name]");
            }
        });
        reg("hudedit", "hudedit - drag HUD elements around", a -> dev.terminal.util.Mc.openScreen(new dev.terminal.gui.HudEditorScreen()));
        reg("guireset", "guireset - reset panel positions and collapsed panels", a -> { dev.terminal.gui.GuiLayout.reset(); ConfigManager.save(); print("GUI layout reset."); });
        reg("save", "save - write config now", a -> { ConfigManager.save(); print("Saved."); });
        reg("clear", "clear - clear this log", a -> LOG.clear());
    }
}
