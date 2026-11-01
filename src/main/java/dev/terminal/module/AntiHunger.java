package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;

/** Singleplayer: keeps the food bar topped up. */
public class AntiHunger extends Module {
    private final NumberSetting minFood = add(new NumberSetting("Min Food", "Refill when food drops below this", 20, 1, 20, 1));

    public AntiHunger() { super("AntiHunger", "Never get hungry (singleplayer)", Category.PLAYER); }

    @Override public void onTick() {
        int min = minFood.intValue();
        Mc.onServerPlayer(sp -> {
            var food = sp.getFoodData();
            if (food.getFoodLevel() < min) { food.setFoodLevel(20); food.setSaturation(5f); }
        });
    }
}
