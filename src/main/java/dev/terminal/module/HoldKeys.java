package dev.terminal.module;

import dev.terminal.util.Mc;
import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.BoolSetting;
import net.minecraft.client.Minecraft;

/** Keeps the attack and/or use button held down (handy for farms and grinders). */
public class HoldKeys extends Module {
    private final BoolSetting attack = add(new BoolSetting("Hold Attack", "Keep attack held", true));
    private final BoolSetting use = add(new BoolSetting("Hold Use", "Keep use held", false));
    private boolean heldAttack, heldUse;

    public HoldKeys() { super("HoldKeys", "Hold attack/use for you", Category.PLAYER); }

    @Override public void onTick() {
        var o = Minecraft.getInstance().options;
        if (Mc.screen() != null) return;
        if (attack.get()) { o.keyAttack.setDown(true); heldAttack = true; } else if (heldAttack) { o.keyAttack.setDown(false); heldAttack = false; }
        if (use.get()) { o.keyUse.setDown(true); heldUse = true; } else if (heldUse) { o.keyUse.setDown(false); heldUse = false; }
    }
    @Override public void onDisable() {
        var o = Minecraft.getInstance().options;
        if (heldAttack) o.keyAttack.setDown(false);
        if (heldUse) o.keyUse.setDown(false);
        heldAttack = heldUse = false;
    }
}
