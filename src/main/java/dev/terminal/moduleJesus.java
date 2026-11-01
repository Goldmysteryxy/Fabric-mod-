package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import net.minecraft.client.Minecraft;

/** Keeps you at the water surface (a gentle bob, not perfectly flat). Sneak to dive. */
public class Jesus extends Module {
    public Jesus() { super("Jesus", "Walk on water", Category.PLAYER); }

    @Override public void onTick() {
        var p = Minecraft.getInstance().player;
        if (p.isInWater() && !p.isShiftKeyDown()) {
            p.setDeltaMovement(p.getDeltaMovement().multiply(1, 0, 1).add(0, 0.1, 0));
            p.fallDistance = 0;
        }
    }
}
