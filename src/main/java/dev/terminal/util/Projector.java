package dev.terminal.util;

import net.minecraft.client.Minecraft;

/**
 * Projects a world position onto the screen using the player's view angles and FOV option.
 * Pure math (no render matrices), so it survives rendering refactors. Slightly approximate
 * (ignores sprint FOV and partial ticks).
 */
public final class Projector {
    /** Returns {screenX, screenY} in GUI-scaled pixels, or null if behind the camera. */
    public static int[] project(double x, double y, double z, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null) return null;
        double yaw = Math.toRadians(p.getYRot()), pitch = Math.toRadians(p.getXRot());
        double fx = -Math.sin(yaw) * Math.cos(pitch), fy = -Math.sin(pitch), fz = Math.cos(yaw) * Math.cos(pitch);
        double rx = -Math.cos(yaw), ry = 0, rz = -Math.sin(yaw);
        double ux = ry * fz - rz * fy, uy = rz * fx - rx * fz, uz = rx * fy - ry * fx;
        double dx = x - p.getX(), dy = y - p.getEyeY(), dz = z - p.getZ();
        double f = dx * fx + dy * fy + dz * fz;
        if (f < 0.1) return null;
        double r = dx * rx + dy * ry + dz * rz;
        double u = dx * ux + dy * uy + dz * uz;
        double tan = Math.tan(Math.toRadians(mc.options.fov().get()) / 2);
        double aspect = (double) sw / sh;
        int px = (int) (sw / 2.0 + r / (f * tan * aspect) * sw / 2.0);
        int py = (int) (sh / 2.0 - u / (f * tan) * sh / 2.0);
        return new int[]{px, py};
    }
}
