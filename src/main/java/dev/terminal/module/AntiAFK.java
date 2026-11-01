package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.EnumSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import java.util.concurrent.ThreadLocalRandom;

public class AntiAFK extends Module {
    private final EnumSetting mode = add(new EnumSetting("Mode", "What to do", "Both", "Look", "Jump", "Both"));
    private final NumberSetting interval = add(new NumberSetting("Interval", "Ticks between actions (20 = 1s)", 600, 100, 2400, 20));
    private int timer;
    private boolean release;

    public AntiAFK() { super("AntiAFK", "Small movements to avoid idle kicks", Category.PLAYER); }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (release) { mc.options.keyJump.setDown(false); release = false; }
        if (--timer > 0) return;
        timer = interval.intValue();
        String m = mode.get();
        if (!m.equals("Jump")) p.setYRot(p.getYRot() + ThreadLocalRandom.current().nextInt(-20, 21));
        if (!m.equals("Look")) { mc.options.keyJump.setDown(true); release = true; }
    }
}
