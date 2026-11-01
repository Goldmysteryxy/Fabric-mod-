package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.ConfigManager;
import dev.terminal.core.Module;
import dev.terminal.core.ModuleManager;
import dev.terminal.core.Terminal;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.StringSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import java.util.*;

/** Friends are skipped by ESP and shown blue on Tracers. Commands: friend add|del|list <name> */
public class Friends extends Module {
    private final StringSetting names = add(new StringSetting("Names", "Friend names, comma separated", ""));
    private final BoolSetting excludeEsp = add(new BoolSetting("Exclude ESP", "Don't outline friends", true));

    public Friends() {
        super("Friends", "Friend list for ESP/Tracers", Category.MISC);
        Terminal.reg("friend", "friend <add|del|list> [name]", this::command);
    }

    private List<String> list() {
        List<String> out = new ArrayList<>();
        for (String s : names.get().split(",")) if (!s.isBlank()) out.add(s.trim());
        return out;
    }
    private void store(List<String> l) { names.set(String.join(",", l)); ConfigManager.save(); }

    private void command(String[] a) {
        List<String> l = list();
        if (a.length < 1) { Terminal.print("friend <add|del|list> [name]"); return; }
        switch (a[0].toLowerCase()) {
            case "list" -> Terminal.print("Friends: " + (l.isEmpty() ? "(none)" : String.join(", ", l)));
            case "add" -> {
                if (a.length < 2 || !a[1].matches("[A-Za-z0-9_]{1,16}")) { Terminal.print("friend add <name>"); return; }
                l.removeIf(n -> n.equalsIgnoreCase(a[1])); l.add(a[1]); store(l); setEnabled(true); Terminal.print("Added " + a[1]);
            }
            case "del" -> {
                if (a.length < 2) { Terminal.print("friend del <name>"); return; }
                boolean r = l.removeIf(n -> n.equalsIgnoreCase(a[1])); store(l); Terminal.print(r ? "Removed " + a[1] : "Not a friend");
            }
            default -> Terminal.print("friend <add|del|list> [name]");
        }
    }

    public static boolean is(Entity e) {
        Friends f = ModuleManager.get(Friends.class);
        if (f == null || !f.isEnabled() || !(e instanceof Player)) return false;
        String n = e.getName().getString();
        for (String s : f.list()) if (s.equalsIgnoreCase(n)) return true;
        return false;
    }
    public static boolean excludeEsp() { Friends f = ModuleManager.get(Friends.class); return f != null && f.excludeEsp.get(); }
}
