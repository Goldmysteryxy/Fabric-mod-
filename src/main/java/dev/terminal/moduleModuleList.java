package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.core.ModuleManager;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.NumberSetting;
import java.util.ArrayList;
import java.util.List;

public class ModuleList extends Module {
    private final BoolSetting hideHud = add(new BoolSetting("Hide HUD", "Skip HUD-category modules", true));
    private final NumberSetting margin = add(new NumberSetting("Margin", "Distance from the right edge", 4, 0, 200, 1));
    private final NumberSetting y = add(new NumberSetting("Y", "Top position", 4, 0, 1080, 2));
    private final ColorSetting color = add(new ColorSetting("Color", "Text color", "Cyan"));

    public ModuleList() { super("ModuleList", "Show enabled modules", Category.HUD); }

    @Override public void onHud(Gfx g) {
        List<String> names = new ArrayList<>();
        for (Module m : ModuleManager.all()) {
            if (!m.isEnabled() || m == this || m instanceof GuiTheme) continue;
            if (hideHud.get() && m.category == Category.HUD) continue;
            names.add(m.name);
        }
        names.sort((a, b) -> g.width(b) - g.width(a));
        int line = 0;
        for (String n : names)
            g.text(n, g.screenW() - margin.intValue() - g.width(n), y.intValue() + 10 * line++, color.color());
    }
}
