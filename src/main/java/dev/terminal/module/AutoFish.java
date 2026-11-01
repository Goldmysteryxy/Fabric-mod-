package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;

/** Hold a fishing rod and cast once. Reels in when the bobber dips, then recasts. */
public class AutoFish extends Module {
    private final NumberSetting sens = add(new NumberSetting("Sensitivity", "Bobber dip that counts as a bite (lower = more sensitive)", 0.15, 0.05, 0.5, 0.01));
    private final NumberSetting recast = add(new NumberSetting("Recast Delay", "Ticks before recasting", 20, 5, 100, 1));
    private double lastY = Double.NaN;
    private int castTicks, cooldown;

    public AutoFish() { super("AutoFish", "Reel in and recast automatically", Category.PLAYER); }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (Mc.screen() != null || !(p.getMainHandItem().is(Items.FISHING_ROD) || p.getOffhandItem().is(Items.FISHING_ROD))) return;
        Entity hook = Mc.hook();
        if (cooldown > 0) {
            if (--cooldown == 0 && hook == null) Mc.useItem();
            return;
        }
        if (hook == null) { lastY = Double.NaN; castTicks = 0; return; }
        castTicks++;
        if (castTicks > 20 && hook.isInWater() && !Double.isNaN(lastY) && lastY - hook.getY() > sens.get()) {
            Mc.useItem();
            cooldown = recast.intValue();
            castTicks = 0;
        }
        lastY = hook.getY();
    }
}
