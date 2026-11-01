package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.BoolSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;

/** Cosmetic option tweaks that are restored when you turn the module off or quit. */
public class VisualTweaks extends Module {
    private final BoolSetting noBob = add(new BoolSetting("No Bobbing", "Turn off view bobbing", true));
    private final BoolSetting minParticles = add(new BoolSetting("Minimal Particles", "Reduce particles", true));
    private Boolean origBob;
    private ParticleStatus origParticles;

    public VisualTweaks() { super("VisualTweaks", "View bobbing and particle tweaks", Category.RENDER); }

    @Override public void onTick() {
        var o = Minecraft.getInstance().options;
        if (noBob.get()) { if (origBob == null) origBob = o.bobView().get(); if (o.bobView().get()) o.bobView().set(false); }
        else restoreBob();
        if (minParticles.get()) {
            if (origParticles == null) origParticles = o.particles().get();
            if (o.particles().get() != ParticleStatus.MINIMAL) o.particles().set(ParticleStatus.MINIMAL);
        } else restoreParticles();
    }

    private void restoreBob() { if (origBob != null) { Minecraft.getInstance().options.bobView().set(origBob); origBob = null; } }
    private void restoreParticles() { if (origParticles != null) { Minecraft.getInstance().options.particles().set(origParticles); origParticles = null; } }

    @Override public void onDisable() { restoreBob(); restoreParticles(); }
    @Override public void onShutdown() { restoreBob(); restoreParticles(); }
}
