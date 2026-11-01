package dev.terminal.mixin;

import dev.terminal.core.ModuleManager;
import dev.terminal.module.Esp;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    // Makes selected entities render with the vanilla glow outline (visible through walls).
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void terminal$esp(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Esp esp = ModuleManager.get(Esp.class);
        if (esp != null && esp.shouldGlow(entity)) cir.setReturnValue(true);
    }
}
