package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;

/** Boosts ground movement. Kept under 1.6x because it is applied every tick. */
public class Speed extends Module {
    private final NumberSetting mult = add(new NumberSetting("Multiplier", "Ground speed multiplier", 1.3, 1.0, 1.6, 0.05));

    public Speed() { super("Speed", "Move faster on the ground", Category.PLAYER); }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        var o = mc.options;
        if (p == null || !p.onGround()) return;
        if (o.keyUp.isDown() || o.keyDown.isDown() || o.keyLeft.isDown() || o.keyRight.isDown())
            p.setDeltaMovement(p.getDeltaMovement().multiply(mult.get(), 1.0, mult.get()));
    }
}
