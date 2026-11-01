package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.BoolSetting;
import net.minecraft.client.Minecraft;

/** Cosmetic option tweaks that are restored when you turn the module off or quit. */
public class VisualTweaks extends Module {
    private final BoolSetting noBob = add(new BoolSetting("No Bobbing", "Turn off view bobbing", true));
    private Boolean origBob;

    public VisualTweaks() { super("VisualTweaks", "View bobbing tweak", Category.RENDER); }

    @Override public void onTick() {
        var o = Minecraft.getInstance().options;
        if (noBob.get()) { if (origBob == null) origBob = o.bobView().get(); if (o.bobView().get()) o.bobView().set(false); }
        else restoreBob();
    }

    private void restoreBob() { if (origBob != null) { Minecraft.getInstance().options.bobView().set(origBob); origBob = null; } }

    @Override public void onDisable() { restoreBob(); }
    @Override public void onShutdown() { restoreBob(); }
}
