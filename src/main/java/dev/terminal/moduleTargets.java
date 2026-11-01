package dev.terminal.module;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

final class Targets {
    static int color(Entity e) {
        return e instanceof Player ? 0xFF55FFFF : e instanceof Enemy ? 0xFFFF5555
                : e instanceof Animal ? 0xFF55FF55 : e instanceof ItemEntity ? 0xFFFFFF55 : 0xFFFFFFFF;
    }
    static boolean matches(Entity e, boolean players, boolean hostile, boolean passive, boolean items) {
        return (players && e instanceof Player) || (hostile && e instanceof Enemy)
                || (passive && e instanceof Animal) || (items && e instanceof ItemEntity);
    }
}
