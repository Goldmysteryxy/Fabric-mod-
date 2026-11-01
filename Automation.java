package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.gui.Gfx;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.ColorSetting;
import dev.terminal.setting.EnumSetting;
import dev.terminal.setting.NumberSetting;
import dev.terminal.setting.StringSetting;
import dev.terminal.util.Mc;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * Does things for you inside your reach: harvest (and replant) crops, mine blocks, and hit mobs.
 * Optional "Seek" walks in a straight line toward the nearest target (no pathfinding).
 * It faces each target before acting so the action matches where you look.
 */
public class Automation extends Module {
    private final BoolSetting farm = add(new BoolSetting("Farm", "Harvest fully grown crops", true));
    private final BoolSetting replant = add(new BoolSetting("Replant", "Replant what you harvested (needs seeds in inventory)", true));
    private final BoolSetting fruit = add(new BoolSetting("Melon Pumpkin", "Also break melon and pumpkin blocks when farming", false));
    private final BoolSetting mine = add(new BoolSetting("Mine", "Break blocks in reach", false));
    private final StringSetting filter = add(new StringSetting("Mine Filter", "Only block ids containing these (comma separated). Empty = anything safe", ""));
    private final BoolSetting hit = add(new BoolSetting("Hit", "Attack mobs in reach", false));
    private final BoolSetting hostile = add(new BoolSetting("Hostile", "Hit hostile mobs", true));
    private final BoolSetting passive = add(new BoolSetting("Passive", "Hit animals (not tamed pets)", false));
    private final BoolSetting players = add(new BoolSetting("Players", "Hit other players (never friends)", false));
    private final EnumSetting priority = add(new EnumSetting("Priority", "Which task goes first", "Hit > Farm > Mine",
            "Hit > Farm > Mine", "Farm > Mine > Hit", "Mine > Farm > Hit"));
    private final NumberSetting range = add(new NumberSetting("Range", "Blocks (never beyond your actual reach)", 4, 1, 6, 1));
    private final NumberSetting delay = add(new NumberSetting("Delay", "Ticks between actions", 2, 1, 10, 1));
    private final BoolSetting autoTool = add(new BoolSetting("Auto Tool", "Switch to the best tool before mining", true));
    private final BoolSetting face = add(new BoolSetting("Face Target", "Turn toward the target before acting", true));
    private final NumberSetting stopHealth = add(new NumberSetting("Pause Health", "Pause when health is below this (0 = never)", 6, 0, 20, 1));
    private final BoolSetting seek = add(new BoolSetting("Seek", "EXPERIMENTAL: walk toward distant targets in a straight line (can walk into lava or off cliffs)", false));
    private final NumberSetting seekRange = add(new NumberSetting("Seek Range", "How far to look for targets", 12, 4, 16, 2));
    private final NumberSetting x = add(new NumberSetting("X", "HUD x position", 300, 0, 1920, 2));
    private final NumberSetting y = add(new NumberSetting("Y", "HUD y position", 120, 0, 1080, 2));
    private final ColorSetting color = add(new ColorSetting("Color", "HUD color", "Yellow"));

    private BlockPos mining;
    private boolean miningFarm;
    private final Map<BlockPos, Item> replantMap = new LinkedHashMap<>();
    private int cooldown, scanTimer;
    private String status = "idle";
    private int farmed, mined, hits;
    private boolean walking;
    private double[] seekTarget;

    public Automation() { super("Automation", "Auto farm, mine and hit in reach", Category.WORLD); }

    // ---------------- helpers ----------------
    private double reach() { return Math.min(range.get(), Minecraft.getInstance().player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)); }
    private double entityReach() { return Math.min(range.get(), Minecraft.getInstance().player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)); }

    private boolean inReach(BlockPos pos, double r) {
        var p = Minecraft.getInstance().player;
        double dx = pos.getX() + 0.5 - p.getX(), dy = pos.getY() + 0.5 - p.getEyeY(), dz = pos.getZ() + 0.5 - p.getZ();
        return dx * dx + dy * dy + dz * dz <= r * r;
    }

    private void aim(double tx, double ty, double tz) {
        if (!face.get()) return;
        var p = Minecraft.getInstance().player;
        double dx = tx - p.getX(), dy = ty - p.getEyeY(), dz = tz - p.getZ();
        double h = Math.sqrt(dx * dx + dz * dz);
        p.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
        p.setXRot((float) -Math.toDegrees(Math.atan2(dy, h)));
    }
    private void aim(BlockPos pos) { aim(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5); }

    private BlockPos nearest(double r, BiPredicate<BlockState, BlockPos> test) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        BlockPos c = p.blockPosition();
        int ri = (int) Math.ceil(r);
        BlockPos.MutableBlockPos q = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bd = Double.MAX_VALUE;
        for (int dx = -ri; dx <= ri; dx++) for (int dy = -ri; dy <= ri; dy++) for (int dz = -ri; dz <= ri; dz++) {
            q.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
            double ex = q.getX() + 0.5 - p.getX(), ey = q.getY() + 0.5 - p.getEyeY(), ez = q.getZ() + 0.5 - p.getZ();
            double d = ex * ex + ey * ey + ez * ez;
            if (d > r * r || d >= bd) continue;
            BlockState s = mc.level.getBlockState(q);
            if (s.isAir() || !test.test(s, q)) continue;
            best = q.immutable();
            bd = d;
        }
        return best;
    }

    private boolean mature(BlockState s, BlockPos q) {
        Block b = s.getBlock();
        if (b instanceof CropBlock c) return c.isMaxAge(s);
        if (b instanceof NetherWartBlock) return s.getValue(NetherWartBlock.AGE) >= 3;
        return fruit.get() && (b == Blocks.MELON || b == Blocks.PUMPKIN);
    }

    private static String[] tokens(String f) { return f.toLowerCase().replace(" ", "").split(","); }

    private boolean mineable(BlockState s, BlockPos q, String[] tokens) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (!s.getFluidState().isEmpty() || s.getDestroySpeed(mc.level, q) < 0) return false;
        Block b = s.getBlock();
        if (farm.get() && (b instanceof CropBlock || b instanceof NetherWartBlock || b instanceof FarmBlock || b instanceof StemBlock)) return false;
        BlockPos feet = p.blockPosition();
        if (q.getX() == feet.getX() && q.getZ() == feet.getZ() && q.getY() < feet.getY()) return false; // don't dig the floor
        for (Direction d : Direction.values()) if (!mc.level.getFluidState(q.relative(d)).isEmpty()) return false; // don't flood yourself
        boolean noFilter = tokens.length == 0 || (tokens.length == 1 && tokens[0].isEmpty());
        if (noFilter) return !s.hasBlockEntity(); // leave chests, furnaces, spawners alone
        String id = BuiltInRegistries.BLOCK.getKey(b).getPath();
        for (String t : tokens) if (!t.isEmpty() && id.contains(t)) return true;
        return false;
    }

    private Entity targetEntity(double r) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        Entity best = null;
        double bd = Double.MAX_VALUE;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == p || !(e instanceof LivingEntity le) || !le.isAlive() || Friends.is(e)) continue;
            boolean ok = (hostile.get() && e instanceof Enemy) || (passive.get() && e instanceof Animal)
                    || (players.get() && e instanceof Player);
            if (!ok || (e instanceof TamableAnimal t && t.isTame())) continue;
            double d = p.distanceTo(e);
            if (d <= r && d < bd) { bd = d; best = e; }
        }
        return best;
    }

    private static Item seedFor(BlockState s) {
        Block b = s.getBlock();
        if (b == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (b == Blocks.CARROTS) return Items.CARROT;
        if (b == Blocks.POTATOES) return Items.POTATO;
        if (b == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        if (b == Blocks.NETHER_WART) return Items.NETHER_WART;
        return null;
    }

    private int findItem(Item item) {
        var inv = Minecraft.getInstance().player.getInventory();
        for (int i = 0; i < 36; i++) if (inv.getItem(i).is(item)) return i;
        return -1;
    }

    // ---------------- tasks ----------------
    private void beginMining(BlockPos pos, BlockState s, boolean isFarm) {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        aim(pos);
        if (autoTool.get()) {
            int cur = Mc.selectedHotbar(), best = cur;
            float bs = p.getInventory().getItem(cur).getDestroySpeed(s);
            for (int i = 0; i < 9; i++) {
                float sp = p.getInventory().getItem(i).getDestroySpeed(s);
                if (sp > bs + 0.01f) { bs = sp; best = i; }
            }
            if (best != cur) Mc.selectHotbar(best);
        }
        Direction face = Direction.getApproximateNearest(p.getX() - pos.getX() - 0.5, p.getEyeY() - pos.getY() - 0.5, p.getZ() - pos.getZ() - 0.5);
        Mc.startDestroy(pos, face);
        p.swing(InteractionHand.MAIN_HAND);
        mining = pos;
        miningFarm = isFarm;
        status = isFarm ? "farming" : "mining";
        cooldown = delay.intValue();
    }

    private boolean tryReplant() {
        if (replantMap.isEmpty()) return false;
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        for (Iterator<Map.Entry<BlockPos, Item>> it = replantMap.entrySet().iterator(); it.hasNext(); ) {
            var e = it.next();
            BlockPos pos = e.getKey();
            if (p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 144) { it.remove(); continue; }
            if (!inReach(pos, reach())) continue;
            if (!mc.level.getBlockState(pos).isAir()) { it.remove(); continue; }
            BlockPos below = pos.below();
            if (mc.level.getBlockState(below).isAir()) { it.remove(); continue; }
            int slot = findItem(e.getValue());
            if (slot < 0) { it.remove(); status = "no seeds to replant"; continue; }
            if (slot >= 9) { Mc.swapSlot(Mc.menuSlot(slot), Mc.selectedHotbar()); cooldown = delay.intValue(); return true; }
            if (Mc.selectedHotbar() != slot) Mc.selectHotbar(slot);
            aim(pos);
            Mc.useItemOn(new BlockHitResult(Vec3.atCenterOf(below).add(0, 0.5, 0), Direction.UP, below, false));
            p.swing(InteractionHand.MAIN_HAND);
            it.remove();
            status = "replanting";
            cooldown = delay.intValue();
            return true;
        }
        return false;
    }

    private boolean tryFarm() {
        if (!farm.get()) return false;
        BlockPos t = nearest(reach(), this::mature);
        if (t == null) return false;
        BlockState s = Minecraft.getInstance().level.getBlockState(t);
        Item seed = seedFor(s);
        if (seed != null && replant.get()) replantMap.put(t, seed);
        beginMining(t, s, true);
        return true;
    }

    private boolean tryMine() {
        if (!mine.get()) return false;
        String[] tk = tokens(filter.get());
        BlockPos t = nearest(reach(), (s, q) -> mineable(s, q, tk));
        if (t == null) return false;
        beginMining(t, Minecraft.getInstance().level.getBlockState(t), false);
        return true;
    }

    private boolean tryHit() {
        if (!hit.get()) return false;
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (p.getAttackStrengthScale(0f) < 1f) return false;
        Entity e = targetEntity(entityReach());
        if (e == null) return false;
        aim(e.getX(), e.getY(0.5), e.getZ());
        Mc.attack(e);
        p.swing(InteractionHand.MAIN_HAND);
        hits++;
        status = "hitting " + e.getName().getString();
        cooldown = delay.intValue();
        return true;
    }

    // ---------------- seeking ----------------
    private void stopWalk() {
        if (!walking) return;
        var o = Minecraft.getInstance().options;
        o.keyUp.setDown(false);
        o.keyJump.setDown(false);
        walking = false;
    }

    private void seekStep() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (scanTimer-- <= 0) {
            scanTimer = 10;
            seekTarget = null;
            double r = seekRange.get(), bd = Double.MAX_VALUE;
            List<double[]> cands = new ArrayList<>();
            if (farm.get()) { BlockPos b = nearest(r, this::mature); if (b != null) cands.add(new double[]{b.getX() + 0.5, b.getY(), b.getZ() + 0.5}); }
            if (mine.get()) { String[] tk = tokens(filter.get()); BlockPos b = nearest(r, (s, q) -> mineable(s, q, tk)); if (b != null) cands.add(new double[]{b.getX() + 0.5, b.getY(), b.getZ() + 0.5}); }
            if (hit.get()) { Entity e = targetEntity(r); if (e != null) cands.add(new double[]{e.getX(), e.getY(), e.getZ()}); }
            for (double[] c : cands) {
                double d = p.distanceToSqr(c[0], c[1], c[2]);
                if (d < bd) { bd = d; seekTarget = c; }
            }
        }
        if (seekTarget == null) { stopWalk(); return; }
        double dx = seekTarget[0] - p.getX(), dz = seekTarget[2] - p.getZ();
        if (Math.sqrt(dx * dx + dz * dz) < 1.2) { seekTarget = null; stopWalk(); return; }
        p.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
        mc.options.keyUp.setDown(true);
        mc.options.keyJump.setDown(p.horizontalCollision && p.onGround());
        walking = true;
        status = "seeking";
    }

    // ---------------- main loop ----------------
    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        var p = mc.player;
        if (mc.screen != null || mc.gameMode == null || p.isDeadOrDying()) { stopWalk(); return; }
        if (stopHealth.get() > 0 && p.getHealth() < stopHealth.get()) { stopWalk(); status = "paused (low health)"; return; }

        if (mining != null) {
            if (mc.level.getBlockState(mining).isAir()) { if (miningFarm) farmed++; else mined++; mining = null; }
            else if (!inReach(mining, reach() + 1)) mining = null;
            else {
                aim(mining);
                Direction face = Direction.getApproximateNearest(p.getX() - mining.getX() - 0.5, p.getEyeY() - mining.getY() - 0.5, p.getZ() - mining.getZ() - 0.5);
                Mc.continueDestroy(mining, face);
                p.swing(InteractionHand.MAIN_HAND);
                stopWalk();
                return;
            }
        }
        if (cooldown-- > 0) return;

        if (replant.get() && tryReplant()) { stopWalk(); return; }
        String[] order = switch (priority.get()) {
            case "Farm > Mine > Hit" -> new String[]{"farm", "mine", "hit"};
            case "Mine > Farm > Hit" -> new String[]{"mine", "farm", "hit"};
            default -> new String[]{"hit", "farm", "mine"};
        };
        for (String task : order) {
            boolean acted = switch (task) { case "hit" -> tryHit(); case "farm" -> tryFarm(); default -> tryMine(); };
            if (acted) { stopWalk(); return; }
        }
        // nothing to do in reach
        cooldown = 3;
        if (seek.get()) seekStep(); else { status = "idle"; stopWalk(); }
        if (!seek.get() || seekTarget == null) status = "idle";
    }

    @Override public void onDisable() {
        mining = null; seekTarget = null; replantMap.clear();
        Mc.stopDestroy();
        stopWalk();
        status = "idle";
    }

    @Override public void onHud(Gfx g) {
        int px = x.intValue(), py = y.intValue();
        g.text("Automation: " + status, px, py, color.color());
        g.text("Farmed " + farmed + "  Mined " + mined + "  Hits " + hits, px, py + 10, 0xFFAAAAAA);
    }
}
