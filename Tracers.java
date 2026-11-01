package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/** Draws a ring of dots around the crosshair pointing toward nearby entities (2D, no world rendering). */
public class Tracers extends Module {
    private final BoolSetting players = add(new BoolSetting("Players", "Track players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile", "Track hostile mobs", true));
    private final BoolSetting passive = add(new BoolSetting("Passive", "Track animals", false));
    private final BoolSetting items = add(new BoolSetting("Items", "Track dropped items", false));
    private final NumberSetting range = add(new NumberSetting("Range", "Max distance in blocks", 48, 8, 256, 8));
    private final NumberSetting radius = add(new NumberSetting("Ring", "Ring radius in pixels", 40, 20, 150, 5));

    public Tracers() { super("Tracers", "Direction dots around the crosshair", Category.HUD); }

    @Override public void onHud(Gfx g) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null || mc.level == null) return;
        int cx = g.screenW() / 2, cy = g.screenH() / 2, r = radius.intValue();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == p || p.distanceTo(e) > range.get()) continue;
            if (!Targets.matches(e, players.get(), hostile.get(), passive.get(), items.get())) continue;
            double dx = e.getX() - p.getX(), dz = e.getZ() - p.getZ();
            double rel = Mth.wrapDegrees(Math.toDegrees(Math.atan2(-dx, dz)) - p.getYRot());
            double rad = Math.toRadians(rel);
            int px = cx + (int) (Math.sin(rad) * r), py = cy - (int) (Math.cos(rad) * r);
            int col = e instanceof Player ? 0xFF55FFFF : e instanceof Enemy ? 0xFFFF5555
                    : e instanceof Animal ? 0xFF55FF55 : e instanceof ItemEntity ? 0xFFFFFF55 : 0xFFFFFFFF;
            if (Friends.is(e)) col = 0xFF5588FF;
            g.rect(px - 2, py - 2, 5, 5, col);
        }
    }
}
