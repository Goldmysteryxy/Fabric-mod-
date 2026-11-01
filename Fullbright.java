package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Client-side night vision (never sent to the server). */
public class Fullbright extends Module {
    public Fullbright() { super("Fullbright", "See in the dark", Category.RENDER); }

    @Override public void onTick() {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        var cur = p.getEffect(MobEffects.NIGHT_VISION);
        if (cur == null || cur.getDuration() < 300)
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));
    }
    @Override public void onDisable() {
        var p = Minecraft.getInstance().player;
        if (p != null) p.removeEffect(MobEffects.NIGHT_VISION);
    }
}
