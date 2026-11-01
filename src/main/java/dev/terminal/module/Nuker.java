package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.setting.StringSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;

/** Mines nearby blocks one at a time using normal mining (so tool speed still matters). */
public class Nuker extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", "Radius in blocks", 4, 1, 6, 1));
    private final StringSetting filter = add(new StringSetting("Filter", "Only block ids containing these (comma separated). Empty = everything", ""));
    private BlockPos current;

    public Nuker() { super("Nuker", "Break nearby blocks automatically", Category.WORLD); }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (Mc.screen() != null || mc.gameMode == null) return;
        double r = range.get();
        if (current != null) {
            boolean gone = mc.level.getBlockState(current).isAir();
            if (gone || p.distanceToSqr(current.getX() + 0.5, current.getY() + 0.5, current.getZ() + 0.5) > (r + 1) * (r + 1)) current = null;
            else { Mc.continueDestroy(current, Direction.UP); p.swing(InteractionHand.MAIN_HAND); return; }
        }
        String[] tokens = filter.get().toLowerCase().replace(" ", "").split(",");
        BlockPos c = p.blockPosition();
        int ri = (int) Math.ceil(r);
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        BlockPos.MutableBlockPos q = new BlockPos.MutableBlockPos();
        for (int dx = -ri; dx <= ri; dx++) for (int dy = -ri; dy <= ri; dy++) for (int dz = -ri; dz <= ri; dz++) {
            double d = dx * dx + dy * dy + dz * dz;
            if (d > r * r || d >= bestD) continue;
            q.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
            BlockState s = mc.level.getBlockState(q);
            if (s.isAir() || !s.getFluidState().isEmpty() || s.getDestroySpeed(mc.level, q) < 0) continue;
            if (!matches(s, tokens)) continue;
            best = q.immutable(); bestD = d;
        }
        if (best != null) { current = best; Mc.startDestroy(best, Direction.UP); p.swing(InteractionHand.MAIN_HAND); }
    }

    private static boolean matches(BlockState s, String[] tokens) {
        if (tokens.length == 0 || (tokens.length == 1 && tokens[0].isEmpty())) return true;
        String id = BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
        for (String t : tokens) if (!t.isEmpty() && id.contains(t)) return true;
        return false;
    }

    @Override public void onDisable() { current = null; Mc.stopDestroy(); }
}
