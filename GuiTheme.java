package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.EnumSetting;
import dev.terminal.setting.NumberSetting;
import java.util.LinkedHashMap;
import java.util.Map;

/** Not a toggle: its settings restyle the Terminal GUI itself. */
public class GuiTheme extends Module {
    // {accent, header, panel bg, text, dim text, row bg, sub-row bg}
    private static final Map<String, int[]> PALETTES = new LinkedHashMap<>();
    static {
        PALETTES.put("Dark",     new int[]{0x2E7D5B, 0x1B1B2B, 0x101018, 0xFFFFFFFF, 0xFFAAAAAA, 0x50000000, 0x60303048});
        PALETTES.put("Midnight", new int[]{0x4F7CFF, 0x0E1230, 0x080A1C, 0xFFFFFFFF, 0xFF9AA5CC, 0x50000010, 0x60202A55});
        PALETTES.put("Forest",   new int[]{0x3FBF6F, 0x16281C, 0x0C1610, 0xFFFFFFFF, 0xFF9CC4A8, 0x50000000, 0x60244030});
        PALETTES.put("Sunset",   new int[]{0xFF8844, 0x2B1520, 0x180C12, 0xFFFFFFFF, 0xFFCCA090, 0x50000000, 0x60503038});
        PALETTES.put("Light",    new int[]{0x3366CC, 0xFFDDDDEE, 0xEEEEF4, 0xFF111111, 0xFF555566, 0x30000000, 0x40888899});
    }

    private final EnumSetting preset = add(new EnumSetting("Preset", "Color scheme (Custom uses Accent/Header below)", "Dark",
            "Dark", "Midnight", "Forest", "Sunset", "Light", "Custom"));
    private final ColorSetting accent = add(new ColorSetting("Accent", "Custom: color of enabled modules", "Green"));
    private final ColorSetting header = add(new ColorSetting("Header", "Custom: panel header color", "Cyan"));
    private final NumberSetting opacity = add(new NumberSetting("Opacity", "Panel opacity (0-255)", 176, 40, 255, 8));
    private final NumberSetting width = add(new NumberSetting("Width", "Panel width in pixels", 120, 100, 180, 5));
    private final NumberSetting rowHeight = add(new NumberSetting("Row Height", "Height of each row", 12, 10, 18, 1));
    private final BoolSetting tooltips = add(new BoolSetting("Tooltips", "Show the description line", true));
    private final BoolSetting terminalLog = add(new BoolSetting("Terminal Log", "Show recent terminal output", true));
    private final BoolSetting dim = add(new BoolSetting("Dim Background", "Darken the game behind the GUI", true));

    public GuiTheme() { super("GuiTheme", "Restyle this GUI (always active)", Category.MISC); }

    private int[] pal() {
        int[] p = PALETTES.get(preset.get());
        if (p != null) return p;
        int a = accent.color() & 0xFFFFFF, h = header.color();
        int hr = (int) (((h >> 16) & 0xFF) * 0.35), hg = (int) (((h >> 8) & 0xFF) * 0.35), hb = (int) ((h & 0xFF) * 0.35);
        return new int[]{a, (hr << 16) | (hg << 8) | hb, 0x101018, 0xFFFFFFFF, 0xFFAAAAAA, 0x50000000, 0x60303048};
    }

    public int accent() { return 0xFF000000 | pal()[0]; }
    public int header() { return 0xFF000000 | pal()[1]; }
    public int panelBg() { return (opacity.intValue() << 24) | pal()[2]; }
    public int text() { return pal()[3]; }
    public int dimText() { return pal()[4]; }
    public int rowBg() { return pal()[5]; }
    public int subBg() { return pal()[6]; }
    public int width() { return width.intValue(); }
    public int rowHeight() { return rowHeight.intValue(); }
    public boolean tooltips() { return tooltips.get(); }
    public boolean terminalLog() { return terminalLog.get(); }
    public boolean dim() { return dim.get(); }
}
