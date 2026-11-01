package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.core.ModuleManager;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import java.util.ArrayDeque;
import java.util.Deque;

/** Popups when modules are toggled. While enabled they replace the action-bar message. */
public class Notifications extends Module {
    private record N(String text, boolean on, long time) {}
    private static final Deque<N> QUEUE = new ArrayDeque<>();
    private final NumberSetting seconds = add(new NumberSetting("Duration", "Seconds on screen", 3, 1, 8, 1));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 300, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 4, 0, 1080, 2));

    public Notifications() { super("Notifications", "Popups when modules toggle", Category.HUD); }

    public static boolean active() {
        Notifications n = ModuleManager.get(Notifications.class);
        return n != null && n.isEnabled();
    }

    public static void push(String module, boolean on) {
        if (!active() || Minecraft.getInstance().player == null) return;
        QUEUE.add(new N(module + (on ? " enabled" : " disabled"), on, System.currentTimeMillis()));
        while (QUEUE.size() > 6) QUEUE.poll();
    }

    @Override public void onHud(Gfx g) {
        long now = System.currentTimeMillis(), life = seconds.intValue() * 1000L;
        while (!QUEUE.isEmpty() && now - QUEUE.peek().time() > life) QUEUE.poll();
        int line = 0;
        for (N n : QUEUE) {
            int w = g.width(n.text()) + 8, by = y.intValue() + line++ * 14;
            g.rect(x.intValue(), by, w, 12, 0xA0000000);
            g.rect(x.intValue(), by, 2, 12, n.on() ? 0xFF55FF55 : 0xFFFF5555);
            g.text(n.text(), x.intValue() + 5, by + 2, 0xFFFFFFFF);
        }
    }
}
