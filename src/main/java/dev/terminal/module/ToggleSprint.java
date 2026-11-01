package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import net.minecraft.client.Minecraft;

public class ToggleSprint extends Module {
    public ToggleSprint() { super("ToggleSprint", "Sprint whenever you walk forward", Category.PLAYER); }

    @Override public void onTick() {
        var o = Minecraft.getInstance().options;
        if (o.keyUp.isDown()) o.keySprint.setDown(true);
    }
    @Override public void onDisable() { Minecraft.getInstance().options.keySprint.setDown(false); }
}
