package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Tweakeroo-style info lines. Toggle each one. */
public class InfoLines extends Module {
    private final BoolSetting biome = add(new BoolSetting("Biome", "Current biome", true));
    private final BoolSetting light = add(new BoolSetting("Light", "Block and sky light at your feet", true));
    private final BoolSetting chunk = add(new BoolSetting("Chunk", "Chunk coordinates", false));
    private final BoolSetting speed = add(new BoolSetting("Speed", "Blocks per second", true));
    private final BoolSetting looking = add(new BoolSetting("Looking At", "Block under the crosshair", true));
    private final BoolSetting ping = add(new BoolSetting("Ping", "Latency", true));
    private final BoolSetting clock = add(new BoolSetting("Clock", "Real time", false));
    private final BoolSetting facing = add(new BoolSetting("Facing", "Yaw and pitch", false));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 4, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 600, 0, 1080, 2));
    private final ColorSetting color = add(new ColorSetting("Color", "Text color", "White"));
    private double lastX, lastZ, bps;
    private boolean have;

    public InfoLines() { super("InfoLines", "Biome, light, speed, ping and more", Category.HUD); }

    @Override public void onTick() {
        var p = Minecraft.getInstance().player;
        if (have) bps = bps * 0.8 + Math.hypot(p.getX() - lastX, p.getZ() - lastZ) * 20 * 0.2;
        lastX = p.getX(); lastZ = p.getZ(); have = true;
    }

    @Override public void onHud(Gfx g) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null || mc.level == null) return;
        var pos = p.blockPosition();
        int px = x.intValue(), py = y.intValue(), col = color.color(), line = 0;
        if (biome.get()) g.text("Biome: " + mc.level.getBiome(pos).getRegisteredName().replace("minecraft:", ""), px, py + 10 * line++, col);
        if (light.get()) g.text("Light: " + mc.level.getBrightness(LightLayer.BLOCK, pos) + " block / " + mc.level.getBrightness(LightLayer.SKY, pos) + " sky", px, py + 10 * line++, col);
        if (chunk.get()) g.text("Chunk: " + (pos.getX() >> 4) + ", " + (pos.getZ() >> 4), px, py + 10 * line++, col);
        if (speed.get()) g.text(String.format(Locale.ROOT, "Speed: %.1f b/s", bps), px, py + 10 * line++, col);
        if (looking.get() && mc.hitResult instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            var bp = bhr.getBlockPos();
            g.text("Looking at: " + mc.level.getBlockState(bp).getBlock().getName().getString() + " " + bp.getX() + " " + bp.getY() + " " + bp.getZ(), px, py + 10 * line++, col);
        }
        if (ping.get()) {
            var conn = mc.getConnection();
            var info = conn == null ? null : conn.getPlayerInfo(p.getUUID());
            if (info != null) g.text("Ping: " + info.getLatency() + " ms", px, py + 10 * line++, col);
        }
        if (clock.get()) g.text("Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), px, py + 10 * line++, col);
        if (facing.get()) g.text(String.format(Locale.ROOT, "Facing: %s (yaw %.0f, pitch %.0f)", p.getDirection().getName(), Mth.wrapDegrees(p.getYRot()), p.getXRot()), px, py + 10 * line, col);
    }
}
