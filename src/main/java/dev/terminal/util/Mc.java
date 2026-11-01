package dev.terminal.util;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import org.lwjgl.glfw.GLFW;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.BlockHitResult;
import java.util.function.Consumer;

/** Version-sensitive helpers live here so a Minecraft update only means editing this file. */
public final class Mc {
    public static Minecraft mc() { return Minecraft.getInstance(); }

    /** Raw key polling (so keybinds don't need to be registered with vanilla controls). */
    public static boolean keyDown(int key) {
        if (key < 0) return false;
        return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(), key) == GLFW.GLFW_PRESS;
    }

    /** Run something on the integrated server's copy of the player (singleplayer only). */
    public static void onServerPlayer(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) return;
        var uuid = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(uuid);
            if (sp != null) action.accept(sp);
        });
    }

    /** Swap inventory slot (9-35 = main inventory) with hotbar index 0-8. Edit here if the click method is renamed. */
    public static void swapSlot(int slot, int hotbar) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.player == null) return;
        mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot, hotbar, ClickType.SWAP, mc.player);
    }

    // ---- thin wrappers around methods that tend to get renamed between versions ----
    public static void click(int containerId, int slot, int button, ClickType type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.player == null) return;
        mc.gameMode.handleInventoryMouseClick(containerId, slot, button, type, mc.player);
    }
    public static void clickInv(int slot, int button, ClickType type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) click(mc.player.inventoryMenu.containerId, slot, button, type);
    }
    public static int selectedHotbar() { return Minecraft.getInstance().player.getInventory().getSelectedSlot(); }
    public static void selectHotbar(int i) { Minecraft.getInstance().player.getInventory().setSelectedSlot(i); }
    public static void useItemOn(BlockHitResult hit) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
    }
    public static void useItem() {
        Minecraft mc = Minecraft.getInstance();
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
    }
    public static void startDestroy(BlockPos p, Direction d) { Minecraft.getInstance().gameMode.startDestroyBlock(p, d); }
    public static void continueDestroy(BlockPos p, Direction d) { Minecraft.getInstance().gameMode.continueDestroyBlock(p, d); }
    public static void stopDestroy() { var gm = Minecraft.getInstance().gameMode; if (gm != null) gm.stopDestroyBlock(); }
    public static Entity hook() { var p = Minecraft.getInstance().player; return p == null ? null : p.fishing; }
    public static boolean mouseDown(int button) {
        return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), button) == GLFW.GLFW_PRESS;
    }
    /** Menu slot id for an Inventory index (0-8 hotbar, 9-35 main). */
    public static int menuSlot(int invIndex) { return invIndex < 9 ? 36 + invIndex : invIndex; }

    /** Open a screen. (26.2+ renamed setScreen -> setScreenAndShow; edit here if that changes again.) */
    public static void openScreen(Screen s) { Minecraft.getInstance().setScreenAndShow(s); }
    public static void openFolder(java.nio.file.Path p) { Util.getPlatform().openPath(p); }
    public static void attack(Entity e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null && mc.player != null) mc.gameMode.attack(mc.player, e);
    }
}
