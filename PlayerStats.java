package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Live stats for the nearest other players: health, armor, held item, distance, status. */
public class PlayerStats extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", "Max distance in blocks", 64, 8, 256, 8));
    private final NumberSetting max = add(new NumberSetting("Max Players", "How many to list", 4, 1, 8, 1));
    private final BoolSetting armor = add(new BoolSetting("Armor", "Show armor points", true));
    private final BoolSetting item = add(new BoolSetting("Item", "Show held item", true));
    private final BoolSetting distance = add(new BoolSetting("Distance", "Show distance", true));
    private final BoolSetting status = add(new BoolSetting("Status", "Show sneaking/sprinting/blocking/gliding, totem, effects", true));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 300, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 60, 0, 1080, 2));

    public PlayerStats() { super("PlayerStats", "Health and stats of nearby players", Category.HUD); }

    @Override public void onHud(Gfx g) {
        Minecraft mc = Minecraft.getInstance();
        var me = mc.player;
        if (me == null || mc.level == null) return;
        List<Player> list = new ArrayList<>();
        for (Player p : mc.level.players())
            if (p != me && !p.isSpectator() && me.distanceTo(p) <= range.get()) list.add(p);
        list.sort(Comparator.comparingDouble(me::distanceTo));

        int px = x.intValue(), py = y.intValue(), line = 0;
        for (int n = 0; n < Math.min(list.size(), max.intValue()); n++) {
            Player p = list.get(n);
            float hp = p.getHealth(), mx = Math.max(1f, p.getMaxHealth()), abs = p.getAbsorptionAmount();
            float f = Math.max(0f, Math.min(1f, hp / mx));
            int hpCol = f > 0.5f ? 0xFF55FF55 : f > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
            String name = p.getName().getString();
            String hpText = String.format(Locale.ROOT, "%.1f/%.0f", hp, mx) + (abs > 0 ? String.format(Locale.ROOT, " +%.0f", abs) : "");

            int ly = py + line * 10;
            g.text(name, px, ly, Friends.is(p) ? 0xFF5588FF : 0xFFFFFFFF);
            int tx = px + g.width(name) + 6;
            g.text(hpText, tx, ly, hpCol);
            g.rect(tx + g.width(hpText) + 4, ly + 3, 40, 3, 0xFF333333);
            g.rect(tx + g.width(hpText) + 4, ly + 3, Math.max(1, (int) (40 * f)), 3, hpCol);
            line++;

            List<String> bits = new ArrayList<>();
            if (armor.get()) bits.add("Armor " + p.getArmorValue());
            if (item.get() && !p.getMainHandItem().isEmpty()) bits.add(p.getMainHandItem().getHoverName().getString());
            if (distance.get()) bits.add(String.format(Locale.ROOT, "%.0fm", me.distanceTo(p)));
            if (status.get()) {
                if (p.isShiftKeyDown()) bits.add("sneaking");
                if (p.isSprinting()) bits.add("sprinting");
                if (p.isFallFlying()) bits.add("gliding");
                if (p.isBlocking()) bits.add("blocking");
                if (p.getOffhandItem().is(Items.TOTEM_OF_UNDYING) || p.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) bits.add("totem");
                if (!p.getActiveEffects().isEmpty()) bits.add(p.getActiveEffects().size() + " fx");
            }
            if (!bits.isEmpty()) g.text("  " + String.join(" | ", bits), px, py + line++ * 10, 0xFFAAAAAA);
        }
        if (list.isEmpty()) g.text("No players nearby", px, py, 0xFF777777);
    }
}
