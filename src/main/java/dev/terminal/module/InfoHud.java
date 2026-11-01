package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import java.util.Locale;

public class InfoHud extends Module {
    private final BoolSetting coords = add(new BoolSetting("Coords", "Show XYZ + facing", true));
    private final BoolSetting fps = add(new BoolSetting("FPS", "Show FPS", true));
    private final BoolSetting armor = add(new BoolSetting("Armor", "Show armor durability", true));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 4, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 4, 0, 1080, 2));
    private final ColorSetting color = add(new ColorSetting("Color", "Text color", "Green"));

    public InfoHud() { super("InfoHud", "Coordinates, FPS and armor", Category.HUD); }

    @Override public void onHud(Gfx g) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null) return;
        int px = x.intValue(), py = y.intValue(), col = color.color(), line = 0;
        if (coords.get()) g.text(String.format(Locale.ROOT, "XYZ %.1f %.1f %.1f  (%s)", p.getX(), p.getY(), p.getZ(), p.getDirection().getName()), px, py + 10 * line++, col);
        if (fps.get()) g.text(mc.getFps() + " FPS", px, py + 10 * line++, col);
        if (armor.get()) {
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack s = p.getItemBySlot(slot);
                if (s.isEmpty() || !s.isDamageableItem()) continue;
                g.text(s.getHoverName().getString() + " " + (s.getMaxDamage() - s.getDamageValue()) + "/" + s.getMaxDamage(), px, py + 10 * line++, col);
            }
        }
    }
}
