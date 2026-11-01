package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import net.minecraft.client.Minecraft;

public class ToggleSneak extends Module {
    public ToggleSneak() { super("ToggleSneak", "Stay sneaking", Category.PLAYER); }
    @Override public void onTick() { if (Minecraft.getInstance().screen == null) Minecraft.getInstance().options.keyShift.setDown(true); }
    @Override public void onDisable() { Minecraft.getInstance().options.keyShift.setDown(false); }
}
