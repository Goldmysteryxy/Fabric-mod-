package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import net.minecraft.client.Minecraft;

/** Stops you walking off edges (like sneaking, without sneaking). Jumping still works. */
public class SafeWalk extends Module {
    public SafeWalk() { super("SafeWalk", "Don't walk off ledges", Category.PLAYER); }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (!p.onGround() || mc.options.keyJump.isDown()) return;
        var next = p.getBoundingBox().move(p.getDeltaMovement().add(0, -0.6, 0));
        if (mc.level.noCollision(p, next)) p.setDeltaMovement(p.getDeltaMovement().multiply(0, 1, 0));
    }
}
