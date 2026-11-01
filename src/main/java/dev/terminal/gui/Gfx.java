package dev.terminal.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Tiny drawing wrapper so modules never touch the (frequently renamed) vanilla graphics class. */
public final class Gfx {
    private final GuiGraphicsExtractor g;
    private final Font font = Minecraft.getInstance().font;

    private int[] track;
    public Gfx(GuiGraphicsExtractor g) { this.g = g; }

    /** Records the bounding box of everything drawn until endTrack() (used by the HUD editor). */
    public void startTrack() { track = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE}; }
    public int[] endTrack() { int[] t = track; track = null; return t == null || t[0] == Integer.MAX_VALUE ? null : t; }
    private void grow(int x1, int y1, int x2, int y2) {
        if (track == null) return;
        track[0] = Math.min(track[0], x1); track[1] = Math.min(track[1], y1);
        track[2] = Math.max(track[2], x2); track[3] = Math.max(track[3], y2);
    }

    public void rect(int x, int y, int w, int h, int argb) { grow(x, y, x + w, y + h); g.fill(x, y, x + w, y + h, argb); }
    public void outline(int x, int y, int w, int h, int argb) {
        rect(x, y, w, 1, argb); rect(x, y + h - 1, w, 1, argb);
        rect(x, y, 1, h, argb); rect(x + w - 1, y, 1, h, argb);
    }
    public void text(String s, int x, int y, int argb) { grow(x, y, x + font.width(s), y + font.lineHeight); g.text(font, s, x, y, argb, true); }
    public int width(String s) { return font.width(s); }
    public int lineHeight() { return font.lineHeight; }
    public int screenW() { return Minecraft.getInstance().getWindow().getGuiScaledWidth(); }
    public int screenH() { return Minecraft.getInstance().getWindow().getGuiScaledHeight(); }
}
