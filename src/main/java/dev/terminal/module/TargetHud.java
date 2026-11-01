package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Name, health and stats of the living entity under your crosshair. */
public class TargetHud extends Module {
    private final BoolSetting armor = add(new BoolSetting("Armor", "Show armor points", true));
    private final BoolSetting item = add(new BoolSetting("Item", "Show held item", true));
    private final BoolSetting distance = add(new BoolSetting("Distance", "Show distance", true));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 4, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 240, 0, 1080, 2));

    public TargetHud() { super("TargetHud", "Health and stats of what you're looking at", Category.HUD); }

    @Override public void onHud(Gfx g) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.hitResult instanceof EntityHitResult ehr) || !(ehr.getEntity() instanceof LivingEntity le)) return;
        float hp = le.getHealth(), max = Math.max(1f, le.getMaxHealth()), abs = le.getAbsorptionAmount();

        String hpText = String.format(Locale.ROOT, "%.1f / %.1f", hp, max) + (abs > 0 ? String.format(Locale.ROOT, "  +%.0f", abs) : "");
        List<String> extra = new ArrayList<>();
        StringBuilder l = new StringBuilder();
        if (armor.get()) l.append("Armor ").append(le.getArmorValue());
        if (item.get() && !le.getMainHandItem().isEmpty()) {
            if (l.length() > 0) l.append("   ");
            l.append(le.getMainHandItem().getHoverName().getString());
        }
        if (l.length() > 0) extra.add(l.toString());
        if (distance.get()) extra.add(String.format(Locale.ROOT, "%.1fm away", mc.player.distanceTo(le)));

        String name = le.getName().getString();
        int w = Math.max(110, Math.max(g.width(name), g.width(hpText)) + 8);
        for (String s : extra) w = Math.max(w, g.width(s) + 8);
        int bx = x.intValue(), by = y.intValue(), h = 28 + extra.size() * 10;

        g.rect(bx, by, w, h, 0x90000000);
        g.text(name, bx + 4, by + 3, 0xFFFFFFFF);
        g.text(hpText, bx + 4, by + 13, 0xFFCCCCCC);
        int ly = by + 23;
        for (String s : extra) { g.text(s, bx + 4, ly, 0xFFAAAAAA); ly += 10; }
        float f = Math.max(0f, Math.min(1f, hp / max));
        g.rect(bx + 2, by + h - 4, w - 4, 3, 0xFF333333);
        g.rect(bx + 2, by + h - 4, (int) ((w - 4) * f), 3, f > 0.5f ? 0xFF55FF55 : f > 0.25f ? 0xFFFFFF55 : 0xFFFF5555);
    }
}
