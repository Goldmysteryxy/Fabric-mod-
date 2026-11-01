package dev.terminal.skin;

import dev.terminal.gui.Gfx;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Main-menu skin library: drop .png files on the window to add, click Apply to switch. */
public class SkinScreen extends Screen {
    private record Hit(int x, int y, int w, int h, Runnable action) {
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }
    private static final int ROW = 28, W = 330;

    private final Screen parent;
    private final List<Hit> hits = new ArrayList<>();
    private String status = "Drop .png skin files onto this window to add them.";
    private boolean busy;
    private int scroll;

    public SkinScreen(Screen parent) { super(Component.literal("Skins")); this.parent = parent; }

    @Override public boolean isPauseScreen() { return false; }

    private void button(Gfx g, int x, int y, int w, String label, int bg, Runnable action, int mx, int my) {
        boolean hov = mx >= x && mx < x + w && my >= y && my < y + 16;
        g.rect(x, y, w, 16, hov ? bg | 0x30000000 : bg);
        g.text(label, x + (w - g.width(label)) / 2, y + 4, 0xFFFFFFFF);
        hits.add(new Hit(x, y, w, 16, action));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
        super.extractRenderState(graphics, mx, my, delta);
        Gfx g = new Gfx(graphics);
        hits.clear();
        int x0 = (this.width - W) / 2;
        List<SkinManager.Skin> skins = SkinManager.list();

        String title = "Skins  " + skins.size() + "/" + SkinManager.max();
        g.text(title, (this.width - g.width(title)) / 2, 10, 0xFFFFFFFF);
        g.text(status, Math.max(4, (this.width - g.width(status)) / 2), 24, busy ? 0xFFFFFF55 : 0xFFAAAAAA);

        int top = 40, bottom = this.height - 30;
        int visibleRows = Math.max(1, (bottom - top) / ROW);
        int maxScroll = Math.max(0, skins.size() - visibleRows);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        for (int i = 0; i < visibleRows && i + scroll < skins.size(); i++) {
            SkinManager.Skin s = skins.get(i + scroll);
            int y = top + i * ROW;
            g.rect(x0, y, W, ROW - 3, 0x90000000);

            int[] face = SkinManager.face(s.file());
            for (int py = 0; py < 8; py++) for (int px = 0; px < 8; px++)
                g.rect(x0 + 4 + px * 3, y + 1 + py * 3, 3, 3, face[py * 8 + px]);

            String name = s.file().length() > 22 ? s.file().substring(0, 19) + "..." : s.file();
            g.text(name, x0 + 34, y + 8, 0xFFFFFFFF);

            int bx = x0 + W - 18;
            button(g, bx, y + 4, 14, "X", 0xC0803030, () -> { SkinManager.delete(s.file()); status = "Deleted " + s.file(); }, mx, my);
            bx -= 48;
            button(g, bx, y + 4, 44, busy ? "..." : "Apply", 0xC02E7D5B, () -> apply(s), mx, my);
            bx -= 58;
            button(g, bx, y + 4, 54, s.variant(), 0xC0404060,
                    () -> SkinManager.setVariant(s.file(), s.variant().equals("slim") ? "classic" : "slim"), mx, my);
        }
        if (skins.isEmpty()) g.text("No skins yet. Drag a 64x64 .png here.", x0 + 4, top + 8, 0xFF888888);

        int by = this.height - 24;
        button(g, x0, by, 90, "Open folder", 0xC0404060, () -> Mc.openFolder(SkinManager.folder()), mx, my);
        button(g, x0 + W - 70, by, 70, "Back", 0xC0404060, this::onClose, mx, my);
    }

    private void apply(SkinManager.Skin s) {
        if (busy) return;
        busy = true;
        status = "Uploading " + s.file() + "...";
        SkinManager.apply(s).thenAccept(err -> Minecraft.getInstance().execute(() -> {
            busy = false;
            status = err == null ? "Applied " + s.file() + "! Rejoin the world (or restart) to see it on your character." : err;
        }));
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        int added = 0;
        String error = null;
        for (Path p : paths) {
            String err = SkinManager.add(p);
            if (err == null) added++; else error = err;
        }
        status = error != null ? error : "Added " + added + " skin" + (added == 1 ? "" : "s") + ".";
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (Mc.mouseButton(event.button()) == 0) {
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (h.in(event.x(), event.y())) { h.action().run(); return true; }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll -= (int) Math.signum(sy);
        return true;
    }

    @Override public void onClose() { Mc.openScreen(parent); }
}
