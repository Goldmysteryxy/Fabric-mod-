package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;

/** Scans nearby blocks and lists the closest ores on the HUD (no world rendering needed). */
public class OreFinder extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", "Scan radius in blocks", 12, 4, 24, 1));
    private final NumberSetting results = add(new NumberSetting("Results", "Lines to show", 6, 1, 12, 1));
    private final BoolSetting coal = add(new BoolSetting("Coal", "Find coal", false));
    private final BoolSetting iron = add(new BoolSetting("Iron", "Find iron", true));
    private final BoolSetting copper = add(new BoolSetting("Copper", "Find copper", false));
    private final BoolSetting gold = add(new BoolSetting("Gold", "Find gold", true));
    private final BoolSetting redstone = add(new BoolSetting("Redstone", "Find redstone", false));
    private final BoolSetting lapis = add(new BoolSetting("Lapis", "Find lapis", false));
    private final BoolSetting diamond = add(new BoolSetting("Diamond", "Find diamond", true));
    private final BoolSetting emerald = add(new BoolSetting("Emerald", "Find emerald", true));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 4, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 80, 0, 1080, 2));
    private final ColorSetting color = add(new ColorSetting("Color", "HUD color", "Cyan"));

    private record Found(String name, double dist, int dx, int dy, int dz) {}
    private List<Found> found = List.of();
    private int timer;
    private static final String[] DIRS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

    public OreFinder() { super("OreFinder", "List nearby ores on the HUD", Category.WORLD); }

    private static final String[] IDS = {"coal_ore", "iron_ore", "copper_ore", "gold_ore", "redstone_ore", "lapis_ore", "diamond_ore", "emerald_ore"};
    private final Map<Block, Integer> kinds = new HashMap<>();

    private int kindOf(Block b) {
        return kinds.computeIfAbsent(b, k -> {
            String id = BuiltInRegistries.BLOCK.getKey(k).getPath();
            for (int i = 0; i < IDS.length; i++) if (id.contains(IDS[i])) return i;
            return -1;
        });
    }

    private boolean wanted(BlockState s) {
        return switch (kindOf(s.getBlock())) {
            case 0 -> coal.get();
            case 1 -> iron.get();
            case 2 -> copper.get();
            case 3 -> gold.get();
            case 4 -> redstone.get();
            case 5 -> lapis.get();
            case 6 -> diamond.get();
            case 7 -> emerald.get();
            default -> false;
        };
    }

    @Override public void onTick() {
        if (timer-- > 0) return;
        timer = 20;
        Minecraft mc = Minecraft.getInstance();
        BlockPos c = mc.player.blockPosition();
        int r = range.intValue();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        List<Found> list = new ArrayList<>();
        for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) for (int dz = -r; dz <= r; dz++) {
            p.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
            BlockState s = mc.level.getBlockState(p);
            if (s.isAir() || !wanted(s)) continue;
            list.add(new Found(s.getBlock().getName().getString(), Math.sqrt(dx * dx + dy * dy + dz * dz), dx, dy, dz));
        }
        list.sort(Comparator.comparingDouble(Found::dist));
        found = list.subList(0, Math.min(list.size(), results.intValue()));
    }

    @Override public void onHud(Gfx g) {
        int px = x.intValue(), py = y.intValue(), col = color.color();
        g.text("Ores nearby:", px, py, col);
        if (found.isEmpty()) { g.text("  none", px, py + 10, 0xFFAAAAAA); return; }
        int line = 1;
        for (Found f : found) {
            double ang = Math.toDegrees(Math.atan2(f.dx(), -f.dz()));
            String dir = DIRS[(int) Math.round(((ang + 360) % 360) / 45) % 8];
            String vert = f.dy() == 0 ? "" : (f.dy() > 0 ? "  +" : "  ") + f.dy() + "y";
            g.text(String.format("  %s  %.0fm %s%s", f.name(), f.dist(), dir, vert), px, py + 10 * line++, col);
        }
    }
}
