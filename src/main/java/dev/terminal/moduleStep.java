package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Walk up blocks without jumping by raising the step-height attribute (client + integrated server). */
public class Step extends Module {
    private final NumberSetting height = add(new NumberSetting("Height", "Blocks you can step up (vanilla = 0.6)", 1.1, 0.6, 4.0, 0.1));

    public Step() { super("Step", "Step up blocks automatically", Category.PLAYER); }

    @Override public void onTick() { apply(height.get()); }
    @Override public void onDisable() { apply(0.6); }
    @Override public void onShutdown() { apply(0.6); }

    private void apply(double h) {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        var a = p.getAttribute(Attributes.STEP_HEIGHT);
        if (a != null && a.getBaseValue() != h) a.setBaseValue(h);
        Mc.onServerPlayer(sp -> {
            var sa = sp.getAttribute(Attributes.STEP_HEIGHT);
            if (sa != null && sa.getBaseValue() != h) sa.setBaseValue(h);
        });
    }
}
