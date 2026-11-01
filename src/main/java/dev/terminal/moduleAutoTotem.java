package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", "Ticks between swaps", 4, 1, 20, 1));
    private int cooldown;

    public AutoTotem() { super("AutoTotem", "Keep a totem in your offhand", Category.PLAYER); }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (mc.screen != null || p.containerMenu != p.inventoryMenu) return;
        if (cooldown-- > 0 || p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
        for (int i = 0; i < 36; i++) {
            if (p.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                Mc.swapSlot(Mc.menuSlot(i), 40); // button 40 = offhand
                cooldown = delay.intValue();
                return;
            }
        }
    }
}
