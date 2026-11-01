package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/** Keeps hotbar stacks topped up from your main inventory (blocks, food, arrows...). */
public class AutoRestock extends Module {
    private final NumberSetting threshold = add(new NumberSetting("Threshold", "Restock when a stack has this many or fewer", 8, 1, 32, 1));
    private final NumberSetting delay = add(new NumberSetting("Delay", "Ticks between swaps", 4, 1, 20, 1));
    private final BoolSetting refillEmpty = add(new BoolSetting("Refill Empty", "Refill a slot after its last item is used up", true));
    private final ItemStack[] last = new ItemStack[9];
    private int cooldown;

    public AutoRestock() { super("AutoRestock", "Refill hotbar from inventory", Category.PLAYER); }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null || mc.gameMode == null || Mc.screen() != null) return;
        if (cooldown-- > 0) return;
        var inv = p.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty()) {
                last[i] = s.copyWithCount(1);
                if (s.isStackable() && s.getCount() <= threshold.intValue() && s.getCount() < s.getMaxStackSize()
                        && swapFrom(last[i], i)) return;
            } else if (refillEmpty.get() && last[i] != null) {
                ItemStack want = last[i];
                last[i] = null;
                if (swapFrom(want, i)) return;
            }
        }
    }

    private boolean swapFrom(ItemStack like, int hotbar) {
        var inv = Minecraft.getInstance().player.getInventory();
        for (int j = 9; j < 36; j++) {
            ItemStack o = inv.getItem(j);
            if (!o.isEmpty() && ItemStack.isSameItemSameComponents(like, o)) {
                Mc.swapSlot(j, hotbar);
                cooldown = delay.intValue();
                return true;
            }
        }
        return false;
    }
}
