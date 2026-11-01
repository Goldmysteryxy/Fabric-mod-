package dev.terminal.gui;

import dev.terminal.core.Category;
import dev.terminal.core.ConfigManager;
import dev.terminal.core.Module;
import dev.terminal.core.ModuleManager;
import dev.terminal.core.Terminal;
import dev.terminal.setting.*;
import dev.terminal.util.Keys;
import dev.terminal.util.Mc;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import java.util.*;

/**
 * Left click module = toggle | right click = show settings | middle click = bind key.
 * Settings are built automatically from each module's Setting list.
 */
public class ClickGuiScreen extends Screen {
    private static final int HEAD = 14, GAP = 6;
    private int pw = 120, row = 12;

    private static dev.terminal.module.GuiTheme theme() { return ModuleManager.get(dev.terminal.module.GuiTheme.class); }
    private static int colOn() { var t = theme(); return t == null ? 0xB02E7D5B : (0xB0 << 24) | (t.accent() & 0xFFFFFF); }
    private static int colRow() { var t = theme(); return t == null ? 0x50000000 : t.rowBg(); }
    private static int colSub() { var t = theme(); return t == null ? 0x60303048 : t.subBg(); }

    private interface Click { void on(int button, double mx, Hit hit); }
    private record Hit(int x, int y, int w, int h, String desc, Click click) {
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }
    private record Row(String label, String value, int bg, double frac, String desc, Click click) {}

    private static final List<String> HISTORY = new ArrayList<>();
    private static final Set<Module> EXPANDED = new HashSet<>();
    private final List<Hit> hits = new ArrayList<>();
    private final Map<Category, Integer> scroll = new EnumMap<>(Category.class);
    private final Map<Category, Integer> maxScroll = new EnumMap<>(Category.class);
    private final Map<Category, int[]> panelBox = new EnumMap<>(Category.class);
    private final StringBuilder input = new StringBuilder();
    private Module bindTarget;
    private dev.terminal.core.MacroManager.Macro bindMacro;
    private int histPos = -1;

    public ClickGuiScreen() { super(Component.literal("Terminal")); }

    @Override public boolean isPauseScreen() { return false; }

    // ---------------- rows ----------------
    private List<Row> macroRows() {
        List<Row> out = new ArrayList<>();
        for (var m : dev.terminal.core.MacroManager.all()) {
            boolean active = dev.terminal.core.MacroManager.isRunning(m) || m.repeating;
            String val = bindMacro == m ? "press a key..." : (m.key >= 0 ? Keys.name(m.key) : (m.repeating ? "rep" : ""));
            out.add(new Row(m.name, val, active ? colOn() : colRow(), -1,
                    String.join("; ", m.cmds) + "   [left: run | right: toggle repeat | middle: bind]",
                    (b, mx, h) -> {
                        if (b == 0) dev.terminal.core.MacroManager.run(m);
                        else if (b == 1 && m.repeat > 0) m.repeating = !m.repeating;
                        else if (b == 2) bindMacro = m;
                    }));
        }
        if (out.isEmpty()) out.add(new Row("(no macros)", "", colRow(), -1,
                "Create one in the terminal:  macro add <name> <cmd; cmd; ...>", (b, mx, h) -> {}));
        return out;
    }

    private List<Row> rows(Category c) {
        if (c == Category.MACROS) return macroRows();
        List<Row> out = new ArrayList<>();
        for (Module m : ModuleManager.all()) {
            if (m.category != c) continue;
            boolean on = m.isEnabled();
            boolean open = EXPANDED.contains(m);
            out.add(new Row((open ? "v " : "> ") + m.name, on ? "ON" : "off", on ? colOn() : colRow(), -1,
                    m.description + "   [left: toggle | right: settings | middle: bind]",
                    (b, mx, h) -> {
                        if (b == 0) m.toggle();
                        else if (b == 1) { if (!EXPANDED.remove(m)) EXPANDED.add(m); }
                        else if (b == 2) bindTarget = m;
                    }));
            if (open) {
                out.add(new Row("  Keybind", bindTarget == m ? "press a key..." : Keys.name(m.keyCode()), colSub(), -1,
                        "Click, then press a key. Backspace clears. Esc cancels.", (b, mx, h) -> bindTarget = m));
                for (Setting<?> s : m.settings) out.add(settingRow(m, s));
            }
        }
        return out;
    }

    private Row settingRow(Module m, Setting<?> s) {
        double frac = -1;
        Click click;
        if (s instanceof BoolSetting b) {
            click = (bt, mx, h) -> b.set(!b.get());
        } else if (s instanceof NumberSetting n) {
            frac = n.frac();
            click = (bt, mx, h) -> { if (bt == 1) n.reset(); else n.setFromFrac((mx - h.x()) / h.w()); };
        } else if (s instanceof EnumSetting e) {
            click = (bt, mx, h) -> e.cycle(bt == 1 ? -1 : 1);
        } else {
            click = (bt, mx, h) -> {
                input.setLength(0);
                input.append("set ").append(m.id()).append(' ').append(s.key()).append(' ');
            };
        }
        return new Row("  " + s.name, s.display(), colSub(), frac, s.description, click);
    }

    // ---------------- render ----------------
    private int lastMy, dragOffX, dragOffY;
    private Category dragCat;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
        var th = theme();
        if (th == null || th.dim()) super.extractRenderState(graphics, mx, my, delta);
        Gfx g = new Gfx(graphics);
        hits.clear();
        lastMy = my;
        if (th != null) { pw = th.width(); row = th.rowHeight(); }
        int panelBg = th == null ? 0xB0101018 : th.panelBg();
        int headCol = th == null ? 0xFF1B1B2B : th.header();
        int txt = th == null ? 0xFFFFFFFF : th.text();
        int dim = th == null ? 0xFFAAAAAA : th.dimText();
        int fill = th == null ? 0x803A6EA5 : (0x80 << 24) | (th.accent() & 0xFFFFFF);

        if (dragCat != null) {
            if (!Mc.leftMouseDown()) { dragCat = null; ConfigManager.save(); }
            else GuiLayout.set(dragCat, Math.max(0, Math.min(mx - dragOffX, this.width - 30)), Math.max(0, Math.min(my - dragOffY, this.height - 60)));
        }

        int bottom = this.height - 74;
        int autoX = 8;
        for (Category c : Category.values()) {
            int[] pos = GuiLayout.get(c);
            int x = pos != null ? pos[0] : autoX;
            int top = pos != null ? pos[1] : 8;
            autoX += pw + GAP;
            boolean collapsed = GuiLayout.collapsed(c);
            List<Row> rows = collapsed ? List.of() : rows(c);
            int contentH = rows.size() * row;
            int avail = Math.max(row, bottom - top - HEAD);
            int viewH = Math.min(contentH, avail);
            int max = Math.max(0, contentH - avail);
            maxScroll.put(c, max);
            int sc = Math.min(scroll.getOrDefault(c, 0), max);
            scroll.put(c, sc);
            panelBox.put(c, new int[]{x, top, pw, HEAD + viewH});

            g.rect(x, top, pw, HEAD, headCol);
            g.text((collapsed ? "+ " : "- ") + c.label, x + 4, top + 3, txt);
            hits.add(new Hit(x, top, pw, HEAD, "Drag to move  |  right-click to collapse", (b, cmx, h) -> {
                if (b == 0) { dragCat = c; dragOffX = (int) cmx - h.x(); dragOffY = lastMy - h.y(); }
                else if (b == 1) GuiLayout.toggleCollapsed(c);
            }));
            if (viewH > 0) g.rect(x, top + HEAD, pw, viewH, panelBg);

            int y = top + HEAD - sc;
            for (Row r : rows) {
                if (y >= top + HEAD && y + row <= top + HEAD + viewH) {
                    g.rect(x, y, pw, row, r.bg());
                    if (r.frac() >= 0) g.rect(x, y, (int) (pw * r.frac()), row, fill);
                    if (mx >= x && mx < x + pw && my >= y && my < y + row) g.rect(x, y, pw, row, 0x30FFFFFF);
                    int ty = y + (row - 8) / 2;
                    g.text(r.label(), x + 3, ty, txt);
                    g.text(r.value(), x + pw - 3 - g.width(r.value()), ty, dim);
                    hits.add(new Hit(x, y, pw, row, r.desc(), r.click()));
                }
                y += row;
            }
        }

        if (th == null || th.tooltips())
            for (Hit h : hits) if (h.in(mx, my)) { g.text(h.desc(), 8, this.height - 72, 0xFFCCCCCC); break; }

        if (th == null || th.terminalLog()) {
            List<String> log = Terminal.LOG;
            int from = Math.max(0, log.size() - 4);
            int ly = this.height - 60;
            for (int i = from; i < log.size(); i++) { g.text(log.get(i), 8, ly, 0xFF9CDCFE); ly += 10; }
        }
        g.rect(6, this.height - 17, this.width - 12, 13, 0xC0000000);
        String cursor = (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : " ";
        g.text("> " + input + cursor, 9, this.height - 14, 0xFF55FF55);
    }

    // ---------------- input ----------------
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int b = Mc.mouseButton(event.button());
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (h.in(mx, my)) { h.click().on(b, mx, h); ConfigManager.save(); return true; }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        for (var en : panelBox.entrySet()) {
            int[] b = en.getValue();
            if (mx >= b[0] && mx < b[0] + b[2] && my >= b[1] && my < b[1] + b[3]) {
                Category c = en.getKey();
                int ns = scroll.getOrDefault(c, 0) - (int) (sy * row * 2);
                scroll.put(c, Math.max(0, Math.min(ns, maxScroll.getOrDefault(c, 0))));
                return true;
            }
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    private static boolean isEnter(int k) { return k == InputConstants.KEY_RETURN || k == InputConstants.KEY_NUMPADENTER; }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        boolean clear = key == InputConstants.KEY_BACKSPACE || key == InputConstants.KEY_DELETE;
        if (bindMacro != null) {
            if (key != InputConstants.KEY_ESCAPE) bindMacro.key = clear ? -1 : key;
            bindMacro = null;
            ConfigManager.save();
            return true;
        }
        if (bindTarget != null) {
            if (key != InputConstants.KEY_ESCAPE) bindTarget.setKeyCode(clear ? -1 : key);
            bindTarget = null;
            ConfigManager.save();
            return true;
        }
        if (key == InputConstants.KEY_ESCAPE) { onClose(); return true; }
        if (isEnter(key)) { submit(); return true; }
        if (key == InputConstants.KEY_BACKSPACE) { if (input.length() > 0) input.setLength(input.length() - 1); return true; }
        if (key == InputConstants.KEY_UP) { history(-1); return true; }
        if (key == InputConstants.KEY_DOWN) { history(1); return true; }
        if (key == ModuleManager.guiKey && input.length() == 0) { onClose(); return true; }
        return super.keyPressed(event);
    }

    @Override
    protected void init() {
        super.init();
        Minecraft.getInstance().onTextInputFocusChange(this, true); // SDL only sends typed text while this is on
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        int cp = event.codepoint();
        if (bindTarget == null && bindMacro == null && cp >= 32 && cp != 127) { input.appendCodePoint(cp); return true; }
        return super.charTyped(event);
    }

    private void submit() {
        String line = input.toString().trim();
        input.setLength(0);
        histPos = -1;
        if (line.isEmpty()) return;
        HISTORY.add(line);
        Terminal.print("> " + line);
        Terminal.run(line);
        ConfigManager.save();
    }

    private void history(int dir) {
        if (HISTORY.isEmpty()) return;
        if (dir < 0) histPos = histPos < 0 ? HISTORY.size() - 1 : Math.max(0, histPos - 1);
        else if (histPos >= 0) histPos++;
        input.setLength(0);
        if (histPos >= HISTORY.size() || histPos < 0) { histPos = -1; return; }
        input.append(HISTORY.get(histPos));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().onTextInputFocusChange(this, false);
        ConfigManager.save();
        super.onClose();
    }
}
