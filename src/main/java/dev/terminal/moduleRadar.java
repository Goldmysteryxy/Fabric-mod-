package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** A small top-down minimap of nearby entities. */
public class Radar extends Module {
    private final BoolSetting players = add(new BoolSetting("Players", "Show players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile", "Show hostile mobs", true));
    private final BoolSetting passive = add(new BoolSetting("Passive", "Show animals", true));
    private final BoolSetting items = add(new BoolSetting("Items", "Show dropped items", false));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", "Up = the way you face (off = north up)", true));
    private final NumberSetting range = add(new NumberSetting("Range", "Blocks shown from center to edge", 32, 8, 128, 8));
    private final NumberSetting size = add(new NumberSetting("Size", "Box size in pixels", 90, 50, 200, 10));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 4, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 130, 0, 1080, 2));

    public Radar() { super("Radar", "Top-down minimap of entities", Category.HUD); }

    @Override public void onHud(Gfx g) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null || mc.level == null) return;
        int s = size.intValue(), bx = x.intValue(), by = y.intValue(), half = s / 2, cx = bx + half, cy = by + half;
        g.rect(bx, by, s, s, 0x80000000);
        g.outline(bx, by, s, s, 0xFFAAAAAA);
        double yaw = Math.toRadians(p.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw), rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == p || !Targets.matches(e, players.get(), hostile.get(), passive.get(), items.get())) continue;
            double dx = e.getX() - p.getX(), dz = e.getZ() - p.getZ();
            double sx, sy;
            if (rotate.get()) { sx = dx * rx + dz * rz; sy = -(dx * fx + dz * fz); } else { sx = dx; sy = dz; }
            int px = cx + (int) (sx / range.get() * half), py = cy + (int) (sy / range.get() * half);
            if (px < bx + 2 || px > bx + s - 3 || py < by + 2 || py > by + s - 3) continue;
            g.rect(px - 1, py - 1, 3, 3, Friends.is(e) ? 0xFF5588FF : Targets.color(e));
        }
        g.rect(cx - 1, cy - 1, 3, 3, 0xFFFFFFFF);
    }
}
