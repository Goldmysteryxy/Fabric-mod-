package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.EnumSetting;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Warns (and optionally switches away) before your tool breaks. */
public class ToolGuard extends Module {
    private final NumberSetting threshold = add(new NumberSetting("Min Durability", "Act at this many uses left", 10, 1, 100, 1));
    private final EnumSetting action = add(new EnumSetting("Action", "What to do", "Switch", "Warn", "Switch"));
    private int warnCd;

    public ToolGuard() { super("ToolGuard", "Don't break your tools", Category.PLAYER); }

    @Override public void onTick() {
        var p = Minecraft.getInstance().player;
        if (warnCd > 0) warnCd--;
        ItemStack s = p.getMainHandItem();
        if (!s.isDamageableItem() || s.getMaxDamage() - s.getDamageValue() > threshold.intValue()) return;
        if (warnCd == 0) {
            p.displayClientMessage(Component.literal("\u00a7cAlmost broken: " + s.getHoverName().getString()), true);
            warnCd = 60;
        }
        if (action.get().equals("Switch")) {
            for (int i = 0; i < 9; i++) {
                ItemStack t = p.getInventory().getItem(i);
                if (t.isEmpty() || !t.isDamageableItem()) { Mc.selectHotbar(i); return; }
            }
        }
    }
}
