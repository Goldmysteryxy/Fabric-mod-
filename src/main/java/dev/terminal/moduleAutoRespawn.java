package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;

public class AutoRespawn extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", "Ticks to wait after dying", 10, 0, 100, 1));
    private int dead;

    public AutoRespawn() { super("AutoRespawn", "Respawn automatically", Category.PLAYER); }

    @Override public void onTick() {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        if (p.isDeadOrDying()) { if (++dead >= delay.intValue()) { p.respawn(); dead = 0; } }
        else dead = 0;
    }
}
