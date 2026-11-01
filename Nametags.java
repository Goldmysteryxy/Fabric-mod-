package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Shows "Zombie 12/20" above mobs by giving the client-side copy a custom name. Players are left alone. */
public class Nametags extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", "Max distance in blocks", 32, 8, 128, 8));
    private final Set<Integer> named = new HashSet<>();
    private int timer;

    public Nametags() { super("Nametags", "Show mob names with health", Category.RENDER); }

    @Override public void onTick() {
        if (timer-- > 0) return;
        timer = 4;
        Minecraft mc = Minecraft.getInstance();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity le) || e instanceof Player) continue;
            if (mc.player.distanceTo(e) > range.get()) { restore(e); continue; }
            if (e.hasCustomName() && !named.contains(e.getId())) continue; // respect real name tags
            String label = String.format(Locale.ROOT, "%s %.0f/%.0f",
                    le.getType().getDescription().getString(), le.getHealth(), le.getMaxHealth());
            le.setCustomName(Component.literal(label));
            le.setCustomNameVisible(true);
            named.add(e.getId());
        }
    }

    private void restore(Entity e) {
        if (named.remove(e.getId())) { e.setCustomName(null); e.setCustomNameVisible(false); }
    }

    @Override public void onDisable() {
        var level = Minecraft.getInstance().level;
        if (level != null) for (int id : named) {
            Entity e = level.getEntity(id);
            if (e != null) { e.setCustomName(null); e.setCustomNameVisible(false); }
        }
        named.clear();
    }
}
