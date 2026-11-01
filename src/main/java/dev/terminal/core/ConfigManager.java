package dev.terminal.core;

import com.google.gson.*;
import dev.terminal.TerminalMod;
import dev.terminal.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path dir() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve("terminalmod");
        try { Files.createDirectories(p.resolve("profiles")); } catch (IOException ignored) {}
        return p;
    }
    private static Path profile(String name) { return dir().resolve("profiles").resolve(name + ".json"); }
    public static boolean validName(String n) { return n.matches("[A-Za-z0-9_-]{1,32}"); }

    public static JsonObject snapshot() {
        JsonObject root = new JsonObject();
        root.addProperty("guiKey", ModuleManager.guiKey);
        JsonObject mods = new JsonObject();
        for (Module m : ModuleManager.all()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.isEnabled());
            o.addProperty("key", m.keyCode());
            JsonObject s = new JsonObject();
            for (Setting<?> st : m.settings) s.add(st.name, st.toJson());
            o.add("settings", s);
            mods.add(m.name, o);
        }
        root.add("modules", mods);
        root.add("macros", MacroManager.toJson());
        root.add("gui", dev.terminal.gui.GuiLayout.toJson());
        return root;
    }

    public static void apply(JsonObject root) {
        if (root.has("guiKey")) ModuleManager.guiKey = root.get("guiKey").getAsInt();
        if (root.has("gui")) dev.terminal.gui.GuiLayout.fromJson(root.getAsJsonObject("gui"));
        if (root.has("macros")) MacroManager.fromJson(root.getAsJsonObject("macros"));
        if (!root.has("modules")) return;
        JsonObject mods = root.getAsJsonObject("modules");
        for (Module m : ModuleManager.all()) {
            if (!mods.has(m.name)) continue;
            try {
                JsonObject o = mods.getAsJsonObject(m.name);
                if (o.has("key")) m.setKeyCode(o.get("key").getAsInt());
                if (o.has("settings")) {
                    JsonObject s = o.getAsJsonObject("settings");
                    for (Setting<?> st : m.settings) {
                        if (!s.has(st.name)) continue;
                        try { st.fromJson(s.get(st.name)); } catch (Exception ignored) {}
                    }
                }
                if (o.has("enabled")) m.setEnabled(o.get("enabled").getAsBoolean());
            } catch (Exception e) { TerminalMod.LOGGER.warn("Bad config for {}", m.name, e); }
        }
    }

    public static void save() { write(dir().resolve("config.json")); }
    public static void load() { read(dir().resolve("config.json")); }

    public static boolean saveProfile(String name) { return write(profile(name)); }
    public static boolean loadProfile(String name) { return Files.exists(profile(name)) && read(profile(name)); }
    public static boolean deleteProfile(String name) {
        try { return Files.deleteIfExists(profile(name)); } catch (IOException e) { return false; }
    }
    public static List<String> listProfiles() {
        List<String> out = new ArrayList<>();
        try (Stream<Path> s = Files.list(dir().resolve("profiles"))) {
            s.forEach(p -> { String f = p.getFileName().toString(); if (f.endsWith(".json")) out.add(f.substring(0, f.length() - 5)); });
        } catch (IOException ignored) {}
        return out;
    }

    private static boolean write(Path p) {
        try { Files.writeString(p, GSON.toJson(snapshot())); return true; }
        catch (IOException e) { TerminalMod.LOGGER.warn("Could not save {}", p, e); return false; }
    }
    private static boolean read(Path p) {
        if (!Files.exists(p)) return false;
        try { apply(JsonParser.parseString(Files.readString(p)).getAsJsonObject()); return true; }
        catch (Exception e) { TerminalMod.LOGGER.warn("Could not load {}", p, e); return false; }
    }
}
