package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import java.util.ArrayDeque;
import java.util.Deque;

/** WASD, space and mouse buttons with clicks-per-second. */
public class Keystrokes extends Module {
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 4, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 340, 0, 1080, 2));
    private final Deque<Long> left = new ArrayDeque<>(), right = new ArrayDeque<>();
    private boolean lastL, lastR;

    public Keystrokes() { super("Keystrokes", "Pressed keys and CPS", Category.HUD); }

    @Override public void onTick() {
        var o = Minecraft.getInstance().options;
        long now = System.currentTimeMillis();
        boolean l = o.keyAttack.isDown(), r = o.keyUse.isDown();
        if (l && !lastL) left.add(now);
        if (r && !lastR) right.add(now);
        lastL = l; lastR = r;
        while (!left.isEmpty() && now - left.peek() > 1000) left.poll();
        while (!right.isEmpty() && now - right.peek() > 1000) right.poll();
    }

    private void key(Gfx g, int bx, int by, int w, String label, boolean down) {
        g.rect(bx, by, w, 18, down ? 0xB0FFFFFF : 0x80000000);
        g.text(label, bx + (w - g.width(label)) / 2, by + 5, down ? 0xFF000000 : 0xFFFFFFFF);
    }

    @Override public void onHud(Gfx g) {
        var o = Minecraft.getInstance().options;
        int bx = x.intValue(), by = y.intValue();
        key(g, bx + 20, by, 18, "W", o.keyUp.isDown());
        key(g, bx, by + 20, 18, "A", o.keyLeft.isDown());
        key(g, bx + 20, by + 20, 18, "S", o.keyDown.isDown());
        key(g, bx + 40, by + 20, 18, "D", o.keyRight.isDown());
        key(g, bx, by + 40, 29, "L " + left.size(), o.keyAttack.isDown());
        key(g, bx + 29, by + 40, 29, "R " + right.size(), o.keyUse.isDown());
        key(g, bx, by + 60, 58, "SPACE", o.keyJump.isDown());
    }
}
