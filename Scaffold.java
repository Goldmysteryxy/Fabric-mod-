package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.NumberSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Places a block under your feet as you move. */
public class Scaffold extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", "Ticks between placements", 1, 1, 10, 1));
    private final BoolSetting switchSlot = add(new BoolSetting("Switch Slot", "Select a block from your hotbar if needed", true));
    private int cooldown;

    public Scaffold() { super("Scaffold", "Place blocks under your feet", Category.PLAYER); }

    private static boolean usable(ItemStack s) {
        if (s.isEmpty() || !(s.getItem() instanceof BlockItem bi)) return false;
        Block b = bi.getBlock();
        return !(b instanceof EntityBlock) && !(b instanceof FallingBlock) && b != Blocks.TNT;
    }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (mc.screen != null || cooldown-- > 0) return;
        BlockPos target = p.blockPosition().below();
        if (!mc.level.getBlockState(target).canBeReplaced()) return;

        int slot = Mc.selectedHotbar();
        if (!usable(p.getInventory().getItem(slot))) {
            if (!switchSlot.get()) return;
            slot = -1;
            for (int i = 0; i < 9; i++) if (usable(p.getInventory().getItem(i))) { slot = i; break; }
            if (slot < 0) return;
            Mc.selectHotbar(slot);
        }
        for (Direction d : Direction.values()) {
            BlockPos n = target.relative(d);
            if (mc.level.getBlockState(n).canBeReplaced()) continue;
            Mc.useItemOn(new BlockHitResult(Vec3.atCenterOf(n), d.getOpposite(), n, false));
            p.swing(InteractionHand.MAIN_HAND);
            cooldown = delay.intValue();
            return;
        }
    }
}
