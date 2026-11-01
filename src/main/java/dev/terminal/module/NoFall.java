package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;

/** Singleplayer: resets fall distance on the integrated server before landing. */
public class NoFall extends Module {
    private final NumberSetting threshold = add(new NumberSetting("Threshold", "Reset once fallen this many blocks", 2, 0, 20, 0.5));

    public NoFall() { super("NoFall", "Cancel fall damage (singleplayer)", Category.PLAYER); }

    @Override public void onTick() {
        var p = Minecraft.getInstance().player;
        if (p == null || p.fallDistance <= threshold.get()) return;
        p.fallDistance = 0;
        Mc.onServerPlayer(sp -> sp.fallDistance = 0);
    }
}
