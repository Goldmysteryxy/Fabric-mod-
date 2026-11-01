package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Eats the best hotbar food when hungry. Holds the use key, so look away from containers/doors. */
public class AutoEat extends Module {
    private final NumberSetting hunger = add(new NumberSetting("Hunger", "Eat when food is at or below this", 14, 1, 19, 1));
    private boolean eating;
    private int prev = -1;

    public AutoEat() { super("AutoEat", "Eat food automatically", Category.PLAYER); }

    private static boolean safe(ItemStack s) {
        return s.has(DataComponents.FOOD) && !s.is(Items.ROTTEN_FLESH) && !s.is(Items.SPIDER_EYE) && !s.is(Items.POISONOUS_POTATO)
                && !s.is(Items.PUFFERFISH) && !s.is(Items.CHORUS_FRUIT) && !s.is(Items.GOLDEN_APPLE) && !s.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (mc.screen != null) { stop(); return; }
        int food = p.getFoodData().getFoodLevel();
        if (eating) {
            if (food >= 20 || !safe(p.getMainHandItem())) stop();
            else { mc.options.keyUse.setDown(true); return; }
        }
        if (food > hunger.intValue()) return;
        int best = -1, bestN = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!safe(s)) continue;
            int n = s.get(DataComponents.FOOD).nutrition();
            if (n > bestN) { bestN = n; best = i; }
        }
        if (best < 0) return;
        prev = Mc.selectedHotbar();
        Mc.selectHotbar(best);
        eating = true;
        mc.options.keyUse.setDown(true);
    }

    private void stop() {
        if (!eating && prev < 0) return;
        Minecraft.getInstance().options.keyUse.setDown(false);
        if (prev >= 0) { Mc.selectHotbar(prev); prev = -1; }
        eating = false;
    }
    @Override public void onDisable() { stop(); }
}
