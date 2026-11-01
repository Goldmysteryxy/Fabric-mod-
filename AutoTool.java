package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.BoolSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** While breaking a block, switches to the hotbar item that mines it fastest. */
public class AutoTool extends Module {
    private final BoolSetting back = add(new BoolSetting("Switch Back", "Return to your previous slot afterwards", true));
    private int prev = -1;

    public AutoTool() { super("AutoTool", "Pick the best tool for the block", Category.PLAYER); }

    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (mc.screen == null && mc.options.keyAttack.isDown()
                && mc.hitResult instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            var state = mc.level.getBlockState(bhr.getBlockPos());
            int cur = Mc.selectedHotbar(), best = cur;
            float bestSpeed = p.getInventory().getItem(cur).getDestroySpeed(state);
            for (int i = 0; i < 9; i++) {
                float sp = p.getInventory().getItem(i).getDestroySpeed(state);
                if (sp > bestSpeed + 0.01f) { bestSpeed = sp; best = i; }
            }
            if (best != cur) { if (prev < 0) prev = cur; Mc.selectHotbar(best); }
        } else if (prev >= 0 && back.get()) {
            Mc.selectHotbar(prev);
            prev = -1;
        } else if (!back.get()) prev = -1;
    }
}
