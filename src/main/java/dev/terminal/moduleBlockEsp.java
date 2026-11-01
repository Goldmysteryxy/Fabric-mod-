package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.NumberSetting;
import dev.terminal.setting.StringSetting;
import dev.terminal.util.Projector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/**
 * Marks any blocks you list. Matching is by id fragment: "diamond_ore,chest,spawner".
 * Edit the list in the terminal:  set blockesp blocks diamond_ore,chest
 */
public class BlockEsp extends Module {
    private final StringSetting blocks = add(new StringSetting("Blocks", "Comma-separated block id fragments", "diamond_ore,ancient_debris,spawner,chest"));
    private final NumberSetting range = add(new NumberSetting("Range", "Scan radius in blocks", 16, 4, 32, 4));
    private final NumberSetting max = add(new NumberSetting("Max", "Max markers", 40, 5, 200, 5));
    private final BoolSetting labels = add(new BoolSetting("Labels", "Show name and distance", true));
    private final ColorSetting color = add(new ColorSetting("Color", "Marker color", "Yellow"));

    private record Found(BlockPos pos, String name, double dist) {}
    private List<Found> found = List.of();
    private final Map<Block, Boolean> cache = new HashMap<>();
    private String lastTokens = "";
    private int timer;

    public BlockEsp() { super("BlockEsp", "Mark chosen blocks on screen", Category.WORLD); }

    private boolean matches(Block b, String[] tokens) {
        return cache.computeIfAbsent(b, k -> {
            String id = BuiltInRegistries.BLOCK.getKey(k).getPath();
            for (String t : tokens) if (!t.isEmpty() && id.contains(t)) return true;
            return false;
        });
    }

    @Override public void onTick() {
        if (timer-- > 0) return;
        timer = 30;
        Minecraft mc = Minecraft.getInstance();
        String tk = blocks.get().toLowerCase().replace(" ", "");
        if (!tk.equals(lastTokens)) { cache.clear(); lastTokens = tk; }
        String[] tokens = tk.split(",");
        BlockPos c = mc.player.blockPosition();
        int r = range.intValue();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        List<Found> list = new ArrayList<>();
        for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) for (int dz = -r; dz <= r; dz++) {
            p.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
            BlockState s = mc.level.getBlockState(p);
            if (s.isAir() || !matches(s.getBlock(), tokens)) continue;
            list.add(new Found(p.immutable(), s.getBlock().getName().getString(), Math.sqrt(dx * dx + dy * dy + dz * dz)));
        }
        list.sort(Comparator.comparingDouble(Found::dist));
        found = list.subList(0, Math.min(list.size(), max.intValue()));
    }

    @Override public void onHud(Gfx g) {
        int col = color.color();
        for (Found f : found) {
            int[] s = Projector.project(f.pos().getX() + 0.5, f.pos().getY() + 0.5, f.pos().getZ() + 0.5, g.screenW(), g.screenH());
            if (s == null) continue;
            g.outline(s[0] - 4, s[1] - 4, 9, 9, col);
            if (labels.get()) g.text(String.format(Locale.ROOT, "%s %.0fm", f.name(), f.dist()), s[0] + 7, s[1] - 4, col);
        }
    }
}
