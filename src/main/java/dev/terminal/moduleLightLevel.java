package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Projector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import java.util.ArrayList;
import java.util.List;

/** Marks floor spots where hostile mobs can spawn (block light 0). */
public class LightLevel extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", "Scan radius", 8, 4, 12, 1));
    private final BoolSetting sky = add(new BoolSetting("Require Dark Sky", "Only spots with no sky light either (safe at any time of day)", true));
    private final NumberSetting max = add(new NumberSetting("Max", "Max markers", 150, 20, 400, 10));
    private List<BlockPos> spots = List.of();
    private int timer;

    public LightLevel() { super("LightLevel", "Show where mobs can spawn", Category.WORLD); }

    @Override public void onTick() {
        if (timer-- > 0) return;
        timer = 20;
        Minecraft mc = Minecraft.getInstance();
        var level = mc.level;
        BlockPos c = mc.player.blockPosition();
        int r = range.intValue();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        List<BlockPos> out = new ArrayList<>();
        for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) for (int dz = -r; dz <= r; dz++) {
            p.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
            if (!level.getBlockState(p).isAir()) continue;
            BlockPos below = p.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
            if (level.getBrightness(LightLayer.BLOCK, p) != 0) continue;
            if (sky.get() && level.getBrightness(LightLayer.SKY, p) != 0) continue;
            out.add(p.immutable());
            if (out.size() >= max.intValue()) break;
        }
        spots = out;
    }

    @Override public void onHud(Gfx g) {
        for (BlockPos s : spots) {
            int[] sc = Projector.project(s.getX() + 0.5, s.getY() + 0.05, s.getZ() + 0.5, g.screenW(), g.screenH());
            if (sc != null) { g.rect(sc[0] - 1, sc[1] - 3, 3, 7, 0xFFFF3333); g.rect(sc[0] - 3, sc[1] - 1, 7, 3, 0xFFFF3333); }
        }
    }
}
