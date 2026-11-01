package dev.terminal.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.terminal.util.Keys;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.util.*;

/**
 * Macros = named lists of terminal commands. Run with a key, the terminal, or the GUI.
 * "wait <ticks>" pauses a running macro. Any command you register works inside macros.
 */
public final class MacroManager {
    public static final class Macro {
        public final String name;
        public final List<String> cmds = new ArrayList<>();
        public int key = -1;
        public int repeat = 0;          // ticks between automatic runs (0 = not repeatable)
        public boolean repeating;
        int sinceRun;
        Macro(String name) { this.name = name; }
    }

    private static final class Runner {
        final Macro macro; int idx; int wait;
        Runner(Macro m) { macro = m; }
    }

    private static final Map<String, Macro> MACROS = new LinkedHashMap<>();
    private static final List<Runner> RUNNING = new ArrayList<>();
    private static final Map<Macro, Boolean> DOWN = new HashMap<>();

    public static Collection<Macro> all() { return MACROS.values(); }
    public static Macro get(String name) { return MACROS.get(name.toLowerCase()); }
    public static boolean isRunning(Macro m) { for (Runner r : RUNNING) if (r.macro == m) return true; return false; }

    // ---------------- running ----------------
    public static void run(Macro m) {
        if (isRunning(m) || RUNNING.size() >= 16) return; // guards against macros that call themselves
        Runner r = new Runner(m);
        RUNNING.add(r);
        advance(r);
    }

    private static void advance(Runner r) {
        List<String> cmds = r.macro.cmds;
        while (r.idx < cmds.size()) {
            String line = cmds.get(r.idx++).trim();
            if (line.isEmpty()) continue;
            if (line.toLowerCase().startsWith("wait")) {
                String[] p = line.split("\\s+");
                int t = 20;
                try { if (p.length > 1) t = Integer.parseInt(p[1]); } catch (NumberFormatException ignored) {}
                r.wait = Math.max(1, Math.min(t, 72000));
                return;
            }
            Terminal.run(line);
        }
        RUNNING.remove(r);
    }

    public static void tick() {
        for (Macro m : MACROS.values()) {
            if (m.repeating && m.repeat > 0 && ++m.sinceRun >= m.repeat) { m.sinceRun = 0; run(m); }
        }
        for (Runner r : new ArrayList<>(RUNNING)) {
            if (!RUNNING.contains(r)) continue;
            if (r.wait > 0 && --r.wait > 0) continue;
            r.wait = 0;
            advance(r);
        }
    }

    public static void pollKeys(Minecraft mc, boolean free) {
        for (Macro m : MACROS.values()) {
            if (m.key < 0) continue;
            boolean d = Mc.keyDown(m.key);
            if (free && d && !DOWN.getOrDefault(m, false)) {
                run(m);
                if (mc.player != null) Mc.overlay(Component.literal("Macro " + m.name + " ran"));
            }
            DOWN.put(m, d);
        }
    }

    // ---------------- persistence ----------------
    public static JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (Macro m : MACROS.values()) {
            JsonObject o = new JsonObject();
            JsonArray a = new JsonArray();
            m.cmds.forEach(a::add);
            o.add("commands", a);
            o.addProperty("key", m.key);
            o.addProperty("repeat", m.repeat);
            root.add(m.name, o);
        }
        return root;
    }

    public static void fromJson(JsonObject root) {
        MACROS.clear(); RUNNING.clear(); DOWN.clear();
        for (String name : root.keySet()) {
            try {
                JsonObject o = root.getAsJsonObject(name);
                Macro m = new Macro(name);
                o.getAsJsonArray("commands").forEach(e -> m.cmds.add(e.getAsString()));
                if (o.has("key")) m.key = o.get("key").getAsInt();
                if (o.has("repeat")) m.repeat = o.get("repeat").getAsInt();
                MACROS.put(name.toLowerCase(), m);
            } catch (Exception ignored) {}
        }
    }

    // ---------------- terminal commands ----------------
    private static boolean validName(String n) { return n.matches("[A-Za-z0-9_-]{1,24}"); }

    public static void init() {
        Terminal.reg("wait", "wait <ticks> - pause (only works inside a macro)",
                a -> Terminal.print("'wait' only works inside a macro (20 ticks = 1 second)."));
        Terminal.reg("macro", "macro <add|run|show|bind|repeat|del|list> ...", MacroManager::command);
    }

    private static void command(String[] a) {
        if (a.length < 1) { help(); return; }
        String sub = a[0].toLowerCase();
        switch (sub) {
            case "list" -> {
                if (MACROS.isEmpty()) Terminal.print("No macros. Try: macro add pvp toggle esp; toggle tracers");
                for (Macro m : MACROS.values())
                    Terminal.print(m.name + "  key=" + Keys.name(m.key) + "  " + m.cmds.size() + " cmds"
                            + (m.repeat > 0 ? "  every " + m.repeat + "t" + (m.repeating ? " (on)" : "") : ""));
            }
            case "add" -> {
                if (a.length < 3 || !validName(a[1])) { Terminal.print("macro add <name> <cmd; cmd; ...>  (name: letters, digits, - _)"); return; }
                Macro m = MACROS.computeIfAbsent(a[1].toLowerCase(), k -> new Macro(a[1]));
                m.cmds.clear();
                for (String c : String.join(" ", Arrays.copyOfRange(a, 2, a.length)).split(";")) if (!c.isBlank()) m.cmds.add(c.trim());
                ConfigManager.save();
                Terminal.print("Macro '" + m.name + "' saved (" + m.cmds.size() + " commands). Bind it: macro bind " + m.name + " <key>");
            }
            case "run", "show", "bind", "repeat", "del" -> {
                if (a.length < 2) { Terminal.print("macro " + sub + " <name>"); return; }
                Macro m = get(a[1]);
                if (m == null) { Terminal.print("No macro '" + a[1] + "'. Try 'macro list'."); return; }
                switch (sub) {
                    case "run" -> run(m);
                    case "show" -> { int i = 1; for (String c : m.cmds) Terminal.print((i++) + ". " + c); }
                    case "del" -> { MACROS.remove(m.name.toLowerCase()); RUNNING.removeIf(r -> r.macro == m); ConfigManager.save(); Terminal.print("Deleted " + m.name); }
                    case "bind" -> {
                        if (a.length < 3) { Terminal.print("macro bind <name> <key|none>"); return; }
                        int k = Keys.parse(a[2]);
                        if (k == Keys.INVALID) { Terminal.print("Unknown key '" + a[2] + "'"); return; }
                        m.key = k; ConfigManager.save(); Terminal.print(m.name + " bound to " + Keys.name(k));
                    }
                    case "repeat" -> {
                        if (a.length < 3) { Terminal.print("macro repeat <name> <ticks|off>"); return; }
                        if (a[2].equalsIgnoreCase("off")) { m.repeating = false; Terminal.print(m.name + " repeat off"); }
                        else try {
                            m.repeat = Math.max(5, Math.min(72000, Integer.parseInt(a[2])));
                            m.repeating = true; m.sinceRun = 0;
                            Terminal.print(m.name + " repeats every " + m.repeat + " ticks");
                        } catch (NumberFormatException e) { Terminal.print("Ticks must be a number (20 = 1s)"); }
                        ConfigManager.save();
                    }
                    default -> {}
                }
            }
            default -> help();
        }
    }

    private static void help() {
        Terminal.print("macro add <name> <cmd; cmd; ...> | run <name> | show <name> | bind <name> <key>");
        Terminal.print("macro repeat <name> <ticks|off> | del <name> | list      (use 'wait 20' between steps)");
    }
}
