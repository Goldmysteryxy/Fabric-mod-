package dev.terminal;

import dev.terminal.core.ConfigManager;
import dev.terminal.core.MacroManager;
import dev.terminal.core.ModuleManager;
import dev.terminal.core.Terminal;
import dev.terminal.gui.Gfx;
import dev.terminal.module.ChatTweaks;
import dev.terminal.module.Modules;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import dev.terminal.skin.SkinManager;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalMod implements ClientModInitializer {
    public static final String ID = "terminalmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    @Override
    public void onInitializeClient() {
        Modules.registerAll();
        Terminal.init();
        MacroManager.init();
        SkinManager.init();
        ConfigManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> ModuleManager.tick());

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(ID, "hud"),
                (graphics, tickCounter) -> ModuleManager.renderHud(new Gfx(graphics)));

        // chat hooks (game/system messages)
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> {
            ChatTweaks c = ModuleManager.get(ChatTweaks.class);
            return c == null || overlay || c.allow(msg);
        });
        ClientReceiveMessageEvents.MODIFY_GAME.register((msg, overlay) -> {
            ChatTweaks c = ModuleManager.get(ChatTweaks.class);
            return c == null ? msg : c.modify(msg, overlay);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ConfigManager.save();
            ModuleManager.shutdown();
        });
        LOGGER.info("Terminal loaded with {} modules", ModuleManager.all().size());
    }
}
