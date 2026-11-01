package dev.terminal.gui;

import dev.terminal.core.ConfigManager;
import dev.terminal.core.Module;
import dev.terminal.core.ModuleManager;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.Map;

/** Drag enabled HUD elements (anything with X/Y settings) to reposition them. Esc to finish. */
public class HudEditorScreen extends Screen {
    private Module dragging;
    private int offX, offY;

    public HudEditorScreen() { super(Component.literal("HUD Editor")); }

    @Override public boolean isPauseScreen() { return false; }

    private static boolean movable(Module m) {
        return m.find("x") instanceof NumberSetting && m.find("y") instanceof NumberSetting;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
        super.extractRenderState(graphics, mx, my, delta);
        Gfx g = new Gfx(graphics);
        g.text("HUD Editor: drag elements, Esc to finish. (Enable a HUD module to see it here.)", 8, 8, 0xFFFFFFFF);
        if (dragging != null) {
            if (!Mc.mouseDown(0)) { dragging = null; ConfigManager.save(); }
            else move(dragging, mx, my);
        }
        for (Map.Entry<Module, int[]> e : ModuleManager.hudBounds().entrySet()) {
            if (!movable(e.getKey())) continue;
            int[] b = e.getValue();
            g.outline(b[0] - 2, b[1] - 2, b[2] - b[0] + 4, b[3] - b[1] + 4, e.getKey() == dragging ? 0xFFFFFF55 : 0xFF55AAFF);
            g.text(e.getKey().name, b[0], b[1] - 12, 0xFF55AAFF);
        }
    }

    private void move(Module m, int mx, int my) {
        int[] b = ModuleManager.hudBounds().get(m);
        if (b == null) return;
        NumberSetting nx = (NumberSetting) m.find("x"), ny = (NumberSetting) m.find("y");
        nx.set(nx.get() + (mx - offX - b[0]));
        ny.set(ny.get() + (my - offY - b[1]));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (Map.Entry<Module, int[]> e : ModuleManager.hudBounds().entrySet()) {
                int[] b = e.getValue();
                if (!movable(e.getKey())) continue;
                if (event.x() >= b[0] - 2 && event.x() <= b[2] + 2 && event.y() >= b[1] - 2 && event.y() <= b[3] + 2) {
                    dragging = e.getKey();
                    offX = (int) event.x() - b[0];
                    offY = (int) event.y() - b[1];
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override public void onClose() { ConfigManager.save(); super.onClose(); }
}
