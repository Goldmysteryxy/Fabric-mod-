package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import java.util.Locale;

/**
 * Mace smash-attack readout: fall distance, whether a smash is ready, estimated damage now and at the ground,
 * attack charge, height above ground and wind charges. Uses the vanilla smash formula:
 * 4 per block for the first 3, 2 per block for the next 5, 1 per block after, plus 0.5 per block per Density level.
 */
public class MaceHud extends Module {
    private final BoolSetting onlyMace = add(new BoolSetting("Only With Mace", "Hide unless a mace is in your main hand", true));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 4, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 540, 0, 1080, 2));
    private final ColorSetting color = add(new ColorSetting("Color", "Text color", "Orange"));

    public MaceHud() { super("MaceHud", "Mace smash damage and fall info", Category.HUD); }

    private static double smashBonus(double fd, int density) {
        if (fd <= 1.5) return 0;
        double b = fd <= 3 ? 4 * fd : fd <= 8 ? 12 + 2 * (fd - 3) : 22 + (fd - 8);
        return b + density * 0.5 * fd;
    }

    private static int level(ItemStack s, ResourceKey<Enchantment> key) {
        var e = s.get(DataComponents.ENCHANTMENTS);
        if (e == null) return 0;
        for (var en : e.entrySet()) if (en.getKey().is(key)) return en.getIntValue();
        return 0;
    }

    @Override public void onHud(Gfx g) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null || mc.level == null) return;
        ItemStack main = p.getMainHandItem();
        if (onlyMace.get() && !main.is(Items.MACE)) return;

        double fd = p.fallDistance;
        boolean ready = fd > 1.5 && !p.isFallFlying();
        double base = p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        int density = level(main, Enchantments.DENSITY), breach = level(main, Enchantments.BREACH), wind = level(main, Enchantments.WIND_BURST);

        BlockPos pos = p.blockPosition();
        int ground = 0;
        while (ground < 64 && mc.level.getBlockState(pos.below(ground + 1)).isAir()) ground++;

        int charges = p.getOffhandItem().is(Items.WIND_CHARGE) ? p.getOffhandItem().getCount() : 0;
        for (int i = 0; i < 36; i++) { ItemStack s = p.getInventory().getItem(i); if (s.is(Items.WIND_CHARGE)) charges += s.getCount(); }

        float charge = p.getAttackStrengthScale(0f);
        int col = color.color(), px = x.intValue(), py = y.intValue(), line = 0;
        g.text("Mace", px, py + 10 * line++, col);
        g.text(String.format(Locale.ROOT, "Fall: %.1fm  %s", fd, ready ? "SMASH READY" : "(needs > 1.5)"), px, py + 10 * line++, ready ? 0xFF55FF55 : 0xFFAAAAAA);
        g.text(String.format(Locale.ROOT, "Hit now: ~%.1f", base + smashBonus(ready ? fd : 0, density)), px, py + 10 * line++, 0xFFFFFFFF);
        if (!p.onGround() && ground > 0)
            g.text(String.format(Locale.ROOT, "At ground (%s): ~%.1f", ground >= 64 ? "64+m" : ground + "m", base + smashBonus(fd + ground, density)), px, py + 10 * line++, 0xFFFFFF55);
        g.text("Charge: " + Math.round(charge * 100) + "%", px, py + 10 * line, charge >= 1f ? 0xFF55FF55 : 0xFFFFAA00);
        g.rect(px + 70, py + 10 * line + 3, 40, 3, 0xFF333333);
        g.rect(px + 70, py + 10 * line + 3, Math.max(1, (int) (40 * Math.min(1f, charge))), 3, charge >= 1f ? 0xFF55FF55 : 0xFFFFAA00);
        line++;
        g.text("Wind charges: " + charges, px, py + 10 * line++, 0xFFAAEEFF);
        if (density + breach + wind > 0)
            g.text("Density " + density + "  Breach " + breach + "  Wind Burst " + wind, px, py + 10 * line, 0xFFAA88FF);
    }
}
