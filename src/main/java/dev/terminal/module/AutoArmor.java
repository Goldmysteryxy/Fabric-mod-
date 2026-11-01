package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Equips the sturdiest armor piece in your inventory (scored by max durability; elytra are left alone). */
public class AutoArmor extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", "Ticks between swaps", 6, 1, 40, 1));
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final int[] MENU = {5, 6, 7, 8};
    private int cooldown;

    public AutoArmor() { super("AutoArmor", "Equip the best armor", Category.PLAYER); }

    private static double score(ItemStack s) {
        if (s.isEmpty() || !s.isDamageableItem()) return -1;
        return s.getMaxDamage() + (s.getMaxDamage() - s.getDamageValue()) * 0.001;
    }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (Mc.screen() != null || p.containerMenu != p.inventoryMenu || p.isDeadOrDying()) return;
        if (cooldown-- > 0) return;
        var inv = p.getInventory();
        for (int k = 0; k < 4; k++) {
            ItemStack cur = p.getItemBySlot(SLOTS[k]);
            if (cur.is(Items.ELYTRA)) continue;
            double curScore = score(cur);
            int bestIdx = -1;
            for (int i = 0; i < 36; i++) {
                ItemStack s = inv.getItem(i);
                if (s.is(Items.ELYTRA) || score(s) <= curScore) continue;
                if (p.getEquipmentSlotForItem(s) != SLOTS[k]) continue;
                curScore = score(s); bestIdx = i;
            }
            if (bestIdx >= 0) {
                int from = Mc.menuSlot(bestIdx);
                Mc.clickInv(from, 0, ContainerInput.PICKUP);
                Mc.clickInv(MENU[k], 0, ContainerInput.PICKUP);
                Mc.clickInv(from, 0, ContainerInput.PICKUP);
                cooldown = delay.intValue();
                return;
            }
        }
    }
}
