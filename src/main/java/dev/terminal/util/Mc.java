package dev.terminal.util;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;
import java.util.function.Consumer;

/** Version-sensitive helpers live here so a Minecraft update only means editing this file. */
public final class Mc {
    public static Minecraft mc() { return Minecraft.getInstance(); }

    /** Raw key polling (so keybinds don't need to be registered with vanilla controls). */
    public static boolean keyDown(int key) {
        if (key <= 0) return false;
        return InputConstants.isKeyDown(key);
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
        mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, slot, hotbar, ContainerInput.SWAP, mc.player);
    }

    // ---- thin wrappers around methods that tend to get renamed between versions ----
    public static void click(int containerId, int slot, int button, ContainerInput type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.player == null) return;
        mc.gameMode.handleContainerInput(containerId, slot, button, type, mc.player);
    }
    public static void clickInv(int slot, int button, ContainerInput type) {
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
    public static boolean leftMouseDown() { return Minecraft.getInstance().mouseHandler.isLeftPressed(); }

    /** The open screen. (26.2 moved it from Minecraft.screen to Minecraft.gui.screen().) */
    public static Screen screen() { return Minecraft.getInstance().gui.screen(); }

    /** Action-bar style message. */
    public static void overlay(net.minecraft.network.chat.Component c) {
        var p = Minecraft.getInstance().player;
        if (p != null) p.sendOverlayMessage(c);
    }

    /** Converts a raw mouse button to 0 = left, 1 = right, 2 = middle (26.3 numbers them from 1). */
    public static int mouseButton(int raw) {
        if (raw == InputConstants.MOUSE_BUTTON_LEFT) return 0;
        if (raw == InputConstants.MOUSE_BUTTON_RIGHT) return 1;
        if (raw == InputConstants.MOUSE_BUTTON_MIDDLE) return 2;
        return -1;
    }
    /** Menu slot id for an Inventory index (0-8 hotbar, 9-35 main). */
    public static int menuSlot(int invIndex) { return invIndex < 9 ? 36 + invIndex : invIndex; }

    /** Open a screen. (26.2+ renamed setScreen -> setScreenAndShow; edit here if that changes again.) */
    public static void openScreen(Screen s) { Minecraft.getInstance().setScreenAndShow(s); }
    public static void openFolder(java.nio.file.Path p) { Blaze3D.openPath(p); }
    public static void attack(Entity e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null && mc.player != null) mc.gameMode.attack(mc.player, e);
    }
    /** Swing the main hand. (The two-argument form exists on every recent version.) */
    public static void swing() {
        var p = Minecraft.getInstance().player;
        if (p != null) p.swing(InteractionHand.MAIN_HAND, true);
    }
}
