package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/** Armor (and optionally held items) with durability, a warning color, and enchantments. */
public class ArmorHud extends Module {
    private final BoolSetting enchants = add(new BoolSetting("Enchants", "Show enchantments", true));
    private final BoolSetting shortNames = add(new BoolSetting("Short Names", "Abbreviate enchantments (Prot IV)", true));
    private final BoolSetting hands = add(new BoolSetting("Hands", "Include main hand and offhand", false));
    private final NumberSetting warn = add(new NumberSetting("Warn Below", "Turn red under this durability %", 20, 5, 60, 5));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 4, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 450, 0, 1080, 2));
    private final ColorSetting color = add(new ColorSetting("Color", "Text color", "White"));

    public ArmorHud() { super("ArmorHud", "Armor durability and enchantments", Category.HUD); }

    @Override public void onHud(Gfx g) {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        List<ItemStack> stacks = new ArrayList<>();
        for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
            stacks.add(p.getItemBySlot(s));
        if (hands.get()) { stacks.add(p.getMainHandItem()); stacks.add(p.getOffhandItem()); }

        int px = x.intValue(), py = y.intValue(), line = 0;
        for (ItemStack s : stacks) {
            if (s.isEmpty()) continue;
            int col = color.color();
            String text = s.getHoverName().getString();
            if (s.isDamageableItem()) {
                int max = s.getMaxDamage(), rem = max - s.getDamageValue();
                int pct = Math.round(rem * 100f / max);
                boolean low = pct < warn.intValue();
                if (low) col = 0xFFFF5555;
                text += "  " + rem + "/" + max + " (" + pct + "%)" + (low ? " !" : "");
                int tw = g.width(text);
                g.text(text, px, py + 10 * line, col);
                int barCol = pct > 50 ? 0xFF55FF55 : pct > 25 ? 0xFFFFFF55 : 0xFFFF5555;
                g.rect(px + tw + 4, py + 10 * line + 3, 30, 3, 0xFF333333);
                g.rect(px + tw + 4, py + 10 * line + 3, Math.max(1, 30 * pct / 100), 3, barCol);
            } else {
                g.text(text, px, py + 10 * line, col);
            }
            line++;
            if (enchants.get()) {
                String e = enchantText(s);
                if (!e.isEmpty()) g.text("  " + e, px, py + 10 * line++, 0xFFAA88FF);
            }
        }
    }

    private String enchantText(ItemStack s) {
        var e = s.get(DataComponents.ENCHANTMENTS);
        if (e == null || e.isEmpty()) return "";
        StringJoiner j = new StringJoiner(", ");
        for (var en : e.entrySet()) {
            String full = Enchantment.getFullname(en.getKey(), en.getIntValue()).getString();
            j.add(shortNames.get() ? abbreviate(full) : full);
        }
        return j.toString();
    }

    /** "Fire Aspect II" -> "FA II", "Protection IV" -> "Prot IV", "Mending" -> "Mend". */
    private static String abbreviate(String full) {
        String[] w = full.split(" ");
        String last = w[w.length - 1];
        boolean hasLevel = w.length > 1 && last.matches("[IVXLC]+|\\d+");
        int nameWords = hasLevel ? w.length - 1 : w.length;
        String name;
        if (nameWords == 1) name = w[0].length() > 4 ? w[0].substring(0, 4) : w[0];
        else { StringBuilder sb = new StringBuilder(); for (int i = 0; i < nameWords; i++) sb.append(w[i].charAt(0)); name = sb.toString(); }
        return hasLevel ? name + " " + last : name;
    }
}
