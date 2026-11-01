package dev.terminal.mixin;

import dev.terminal.skin.SkinScreen;
import dev.terminal.util.Mc;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the "Skins" button to the main menu. */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("RETURN"))
    private void terminal$addSkinsButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.literal("Skins"), b -> Mc.openScreen(new SkinScreen(this)))
                .bounds(6, 6, 60, 20).build());
    }
}
