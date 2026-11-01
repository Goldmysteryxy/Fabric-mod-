package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Uses vanilla's glow outline, which renders through walls. The mixin asks shouldGlow(). */
public class Esp extends Module {
    private final BoolSetting players = add(new BoolSetting("Players", "Outline players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile", "Outline hostile mobs", true));
    private final BoolSetting passive = add(new BoolSetting("Passive", "Outline animals", false));
    private final BoolSetting items = add(new BoolSetting("Items", "Outline dropped items", false));
    private final NumberSetting range = add(new NumberSetting("Range", "Max distance in blocks", 64, 8, 256, 8));

    public Esp() { super("ESP", "Outline entities through walls", Category.RENDER); }

    public boolean shouldGlow(Entity e) {
        var p = Minecraft.getInstance().player;
        if (!isEnabled() || p == null || e == p) return false;
        if (p.distanceTo(e) > range.get()) return false;
        if (Friends.is(e) && Friends.excludeEsp()) return false;
        return Targets.matches(e, players.get(), hostile.get(), passive.get(), items.get());
    }
}
