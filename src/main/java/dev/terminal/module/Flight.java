package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;

/** Creative-style flight in survival (double-tap jump). Singleplayer: the integrated server is told too. */
public class Flight extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", "Flying speed (vanilla creative = 0.05)", 0.1, 0.02, 0.5, 0.01));

    public Flight() { super("Flight", "Fly in survival (double-tap jump)", Category.PLAYER); }

    @Override public void onTick() {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        var ab = p.getAbilities();
        ab.mayfly = true;
        ab.setFlyingSpeed(speed.get().floatValue());
        Mc.onServerPlayer(sp -> sp.getAbilities().mayfly = true);
    }

    @Override public void onDisable() {
        var p = Minecraft.getInstance().player;
        if (p == null || p.isCreative() || p.isSpectator()) return;
        var ab = p.getAbilities();
        ab.mayfly = false; ab.flying = false; ab.setFlyingSpeed(0.05f);
        Mc.onServerPlayer(sp -> {
            sp.getAbilities().mayfly = false; sp.getAbilities().flying = false;
            sp.onUpdateAbilities();
        });
    }
}
