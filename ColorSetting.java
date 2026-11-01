package dev.terminal.setting;

public class ColorSetting extends EnumSetting {
    private static final String[] NAMES = {"White", "Green", "Cyan", "Yellow", "Orange", "Red", "Pink"};
    private static final int[] COLORS = {0xFFFFFFFF, 0xFF55FF55, 0xFF55FFFF, 0xFFFFFF55, 0xFFFFAA00, 0xFFFF5555, 0xFFFF77FF};
    public ColorSetting(String name, String desc, String def) { super(name, desc, def, NAMES); }
    public int color() { return COLORS[index()]; }
}
