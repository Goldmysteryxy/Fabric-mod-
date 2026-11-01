package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.EnumSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;

/** Extra crosshair drawn on top of the vanilla one; changes color over entities. */
public class Crosshair extends Module {
    private final EnumSetting style = add(new EnumSetting("Style", "Shape", "Ring", "Dot", "Ring", "Plus"));
    private final ColorSetting color = add(new ColorSetting("Color", "Normal color", "White"));
    private final ColorSetting target = add(new ColorSetting("Target Color", "Color when over an entity", "Red"));
    private final NumberSetting size = add(new NumberSetting("Size", "Size in pixels", 5, 2, 14, 1));

    public Crosshair() { super("Crosshair", "Custom crosshair overlay", Category.HUD); }

    @Override public void onHud(Gfx g) {
        int cx = g.screenW() / 2, cy = g.screenH() / 2, s = size.intValue();
        int col = Minecraft.getInstance().hitResult instanceof EntityHitResult ? target.color() : color.color();
        switch (style.get()) {
            case "Dot" -> g.rect(cx - 1, cy - 1, 3, 3, col);
            case "Plus" -> {
                g.rect(cx - s - 2, cy, s, 1, col); g.rect(cx + 3, cy, s, 1, col);
                g.rect(cx, cy - s - 2, 1, s, col); g.rect(cx, cy + 3, 1, s, col);
            }
            default -> {
                for (int i = 0; i < 12; i++) {
                    double a = Math.toRadians(i * 30);
                    g.rect(cx + (int) Math.round(Math.cos(a) * (s + 3)), cy + (int) Math.round(Math.sin(a) * (s + 3)), 1, 1, col);
                }
            }
        }
    }
}
