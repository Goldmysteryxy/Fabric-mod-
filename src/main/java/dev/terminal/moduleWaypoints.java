package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.ConfigManager;
import dev.terminal.core.Module;
import dev.terminal.core.Terminal;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.util.Projector;
import dev.terminal.setting.StringSetting;
import net.minecraft.client.Minecraft;
import java.util.*;

/** Named markers drawn on screen. Commands: waypoint add <name> | del <name> | list */
public class Waypoints extends Module {
    private final StringSetting data = add(new StringSetting("Data", "Saved waypoints (managed by the 'waypoint' command)", ""));
    private final BoolSetting distance = add(new BoolSetting("Distance", "Show distance", true));
    private final ColorSetting color = add(new ColorSetting("Color", "Marker color", "Pink"));

    public Waypoints() {
        super("Waypoints", "Named markers (use 'waypoint' command)", Category.WORLD);
        Terminal.reg("waypoint", "waypoint <add|del|list> [name]", this::command);
    }

    private record Wp(String name, int x, int y, int z) {}

    private List<Wp> list() {
        List<Wp> out = new ArrayList<>();
        for (String e : data.get().split(";")) {
            String[] p = e.split(":");
            if (p.length != 4) continue;
            try { out.add(new Wp(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]))); }
            catch (NumberFormatException ignored) {}
        }
        return out;
    }

    private void store(List<Wp> l) {
        StringBuilder sb = new StringBuilder();
        for (Wp w : l) sb.append(w.name()).append(':').append(w.x()).append(':').append(w.y()).append(':').append(w.z()).append(';');
        data.set(sb.toString());
        ConfigManager.save();
    }

    private void command(String[] a) {
        if (a.length < 1) { Terminal.print("waypoint <add|del|list> [name]"); return; }
        List<Wp> l = list();
        switch (a[0].toLowerCase()) {
            case "list" -> { if (l.isEmpty()) Terminal.print("No waypoints."); for (Wp w : l) Terminal.print(w.name() + "  " + w.x() + " " + w.y() + " " + w.z()); }
            case "add" -> {
                var p = Minecraft.getInstance().player;
                if (a.length < 2 || !a[1].matches("[A-Za-z0-9_-]{1,24}")) { Terminal.print("Give a name (letters, digits, - _)"); return; }
                if (p == null) return;
                l.removeIf(w -> w.name().equalsIgnoreCase(a[1]));
                l.add(new Wp(a[1], p.blockPosition().getX(), p.blockPosition().getY(), p.blockPosition().getZ()));
                store(l); setEnabled(true); Terminal.print("Added " + a[1]);
            }
            case "del" -> {
                if (a.length < 2) { Terminal.print("waypoint del <name>"); return; }
                boolean removed = l.removeIf(w -> w.name().equalsIgnoreCase(a[1]));
                store(l); Terminal.print(removed ? "Deleted " + a[1] : "No waypoint '" + a[1] + "'");
            }
            default -> Terminal.print("waypoint <add|del|list> [name]");
        }
    }

    @Override public void onHud(Gfx g) {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        for (Wp w : list()) {
            int[] s = Projector.project(w.x() + 0.5, w.y() + 0.5, w.z() + 0.5, g.screenW(), g.screenH());
            if (s == null) continue;
            double d = Math.sqrt(p.distanceToSqr(w.x() + 0.5, w.y() + 0.5, w.z() + 0.5));
            g.rect(s[0] - 2, s[1] - 2, 5, 5, color.color());
            g.text(w.name() + (distance.get() ? String.format(Locale.ROOT, " %.0fm", d) : ""), s[0] + 6, s[1] - 4, color.color());
        }
    }
}
