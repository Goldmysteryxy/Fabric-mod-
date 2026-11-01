package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Hold use with a block in hand to place every few ticks instead of the vanilla delay. */
public class FastPlace extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", "Ticks between placements", 1, 1, 4, 1));
    private int cd;

    public FastPlace() { super("FastPlace", "Place blocks faster", Category.PLAYER); }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (mc.screen != null || !mc.options.keyUse.isDown()) { cd = 0; return; }
        if (cd-- > 0 || !(p.getMainHandItem().getItem() instanceof BlockItem)) return;
        if (!(mc.hitResult instanceof BlockHitResult bhr) || bhr.getType() != HitResult.Type.BLOCK) return;
        if (mc.level.getBlockState(bhr.getBlockPos()).getBlock() instanceof EntityBlock) return; // don't spam chests/furnaces
        Mc.useItemOn(bhr);
        p.swing(InteractionHand.MAIN_HAND);
        cd = delay.intValue() - 1;
    }
}
