package dev.terminal.module;

import dev.terminal.util.Mc;
import dev.terminal.core.Category;
import dev.terminal.core.Module;
import net.minecraft.client.Minecraft;

public class AutoWalk extends Module {
    public AutoWalk() { super("AutoWalk", "Walk forward automatically", Category.PLAYER); }
    @Override public void onTick() { if (Mc.screen() == null) Minecraft.getInstance().options.keyUp.setDown(true); }
    @Override public void onDisable() { Minecraft.getInstance().options.keyUp.setDown(false); }
}
