package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;

public class Spider extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", "Climb speed", 0.2, 0.1, 0.5, 0.05));

    public Spider() { super("Spider", "Climb walls", Category.PLAYER); }

    @Override public void onTick() {
        var p = Minecraft.getInstance().player;
        if (p.horizontalCollision && !p.isInWater()) {
            p.setDeltaMovement(p.getDeltaMovement().multiply(1, 0, 1).add(0, speed.get(), 0));
            p.fallDistance = 0;
        }
    }
}
