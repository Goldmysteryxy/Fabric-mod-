package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;

public class PotionHud extends Module {
    private final BoolSetting duration = add(new BoolSetting("Duration", "Show time remaining", true));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 4, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 40, 0, 1080, 2));
    private final ColorSetting color = add(new ColorSetting("Color", "Text color", "White"));

    public PotionHud() { super("PotionHud", "List active potion effects", Category.HUD); }

    @Override public void onHud(Gfx g) {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        int line = 0;
        for (MobEffectInstance e : p.getActiveEffects()) {
            String name = e.getEffect().value().getDisplayName().getString() + " " + (e.getAmplifier() + 1);
            if (duration.get()) {
                if (e.isInfiniteDuration()) name += "  inf";
                else { int s = e.getDuration() / 20; name += String.format("  %d:%02d", s / 60, s % 60); }
            }
            g.text(name, x.intValue(), y.intValue() + 10 * line++, color.color());
        }
    }
}
