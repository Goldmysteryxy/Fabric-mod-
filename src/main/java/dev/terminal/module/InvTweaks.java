package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.core.Terminal;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/**
 * Enabled: opening a chest/barrel/shulker auto-moves its items into your inventory.
 * Always available: the 'sort' terminal command sorts your main inventory.
 */
public class InvTweaks extends Module {
    private final NumberSetting perTick = add(new NumberSetting("Per Tick", "Stacks moved per tick when looting", 3, 1, 9, 1));

    public InvTweaks() {
        super("InvTweaks", "Quick-loot chests (+ 'sort' command)", Category.MISC);
        Terminal.reg("sort", "sort - sort your main inventory", a -> sort());
    }

    @Override public void onTick() {
        var p = Minecraft.getInstance().player;
        if (!(p.containerMenu instanceof ChestMenu cm) || p.containerMenu == p.inventoryMenu) return;
        int n = cm.getRowCount() * 9, moved = 0;
        for (int i = 0; i < n && moved < perTick.intValue(); i++) {
            if (cm.getSlot(i).hasItem()) { Mc.click(cm.containerId, i, 0, ContainerInput.QUICK_MOVE); moved++; }
        }
    }

    private static String key(ItemStack s) { return s.isEmpty() ? "~" : BuiltInRegistries.ITEM.getKey(s.getItem()).toString(); }

    private static void sort() {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        ItemStack[] cur = new ItemStack[27];
        for (int i = 0; i < 27; i++) cur[i] = p.getInventory().getItem(9 + i).copy();
        List<ItemStack> want = new ArrayList<>(Arrays.asList(cur));
        want.sort(Comparator.comparing(InvTweaks::key).thenComparing((a, b) -> b.getCount() - a.getCount()));
        for (int t = 0; t < 27; t++) {
            if (ItemStack.matches(cur[t], want.get(t))) continue;
            for (int k = t + 1; k < 27; k++) {
                if (!ItemStack.matches(cur[k], want.get(t))) continue;
                Mc.clickInv(9 + t, 0, ContainerInput.PICKUP);
                Mc.clickInv(9 + k, 0, ContainerInput.PICKUP);
                Mc.clickInv(9 + t, 0, ContainerInput.PICKUP);
                ItemStack tmp = cur[t]; cur[t] = cur[k]; cur[k] = tmp;
                break;
            }
        }
        Terminal.print("Inventory sorted.");
    }
}
