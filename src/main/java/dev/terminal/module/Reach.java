package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Raises the interaction-range attributes on the client and the integrated server. */
public class Reach extends Module {
    private final NumberSetting block = add(new NumberSetting("Block Reach", "Block range (vanilla 4.5)", 6, 4.5, 10, 0.5));
    private final NumberSetting entity = add(new NumberSetting("Entity Reach", "Attack range (vanilla 3)", 4.5, 3, 8, 0.5));

    public Reach() { super("Reach", "Longer block and attack range", Category.PLAYER); }

    @Override public void onTick() { apply(block.get(), entity.get()); }
    @Override public void onDisable() { apply(4.5, 3.0); }
    @Override public void onShutdown() { apply(4.5, 3.0); }

    private void apply(double b, double e) {
        var p = Minecraft.getInstance().player;
        if (p == null) return;
        set(p.getAttribute(Attributes.BLOCK_INTERACTION_RANGE), b);
        set(p.getAttribute(Attributes.ENTITY_INTERACTION_RANGE), e);
        Mc.onServerPlayer(sp -> {
            set(sp.getAttribute(Attributes.BLOCK_INTERACTION_RANGE), b);
            set(sp.getAttribute(Attributes.ENTITY_INTERACTION_RANGE), e);
        });
    }
    private static void set(net.minecraft.world.entity.ai.attributes.AttributeInstance a, double v) {
        if (a != null && a.getBaseValue() != v) a.setBaseValue(v);
    }
}
