package dev.terminal.core;

import dev.terminal.TerminalMod;
import dev.terminal.gui.ClickGuiScreen;
import dev.terminal.gui.Gfx;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    private static final Map<Module, Boolean> DOWN = new HashMap<>();
    public static int guiKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private static boolean guiDown;
    private static final Map<Module, int[]> BOUNDS = new HashMap<>();
    public static Map<Module, int[]> hudBounds() { return BOUNDS; }

    public static void register(Module m) { MODULES.add(m); }
    public static List<Module> all() { return MODULES; }

    public static <T extends Module> T get(Class<T> type) {
        for (Module m : MODULES) if (type.isInstance(m)) return type.cast(m);
        return null;
    }

    public static Module find(String query) {
        String q = query.toLowerCase().replace(" ", "");
        for (Module m : MODULES) if (m.id().equals(q)) return m;
        return null;
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        pollKeys(mc);
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            try { m.onTick(); }
            catch (Throwable t) {
                TerminalMod.LOGGER.error("Module {} crashed, disabling", m.name, t);
                Terminal.print("! " + m.name + " crashed and was disabled: " + t);
                m.setEnabled(false);
            }
        }
        MacroManager.tick();
    }

    private static void pollKeys(Minecraft mc) {
        boolean free = mc.screen == null;
        boolean g = Mc.keyDown(guiKey);
        if (free && g && !guiDown) Mc.openScreen(new ClickGuiScreen());
        guiDown = g;
        for (Module m : MODULES) {
            if (m.keyCode() < 0) continue;
            boolean d = Mc.keyDown(m.keyCode());
            if (free && d && !DOWN.getOrDefault(m, false)) {
                m.toggle();
                if (!dev.terminal.module.Notifications.active()) mc.player.displayClientMessage(
                        Component.literal(m.name + (m.isEnabled() ? " \u00a7aenabled" : " \u00a7cdisabled")), true);
            }
            DOWN.put(m, d);
        }
        MacroManager.pollKeys(mc, free);
    }

    public static void renderHud(Gfx g) {
        for (Module m : MODULES) {
            if (!m.isEnabled()) { BOUNDS.remove(m); continue; }
            g.startTrack();
            try { m.onHud(g); } catch (Throwable ignored) {}
            int[] b = g.endTrack();
            if (b != null) BOUNDS.put(m, b); else BOUNDS.remove(m);
        }
    }

    public static void shutdown() {
        for (Module m : MODULES) { try { m.onShutdown(); } catch (Throwable ignored) {} }
    }
}
