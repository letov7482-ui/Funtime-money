package com.axiom.funtime.modules;

import com.axiom.funtime.FunTimeMod;
import com.axiom.funtime.utils.Pathfinder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

public class AutoMiner {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final Set<Block> VALUABLE = Set.of(
        Blocks.ANCIENT_DEBRIS, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.NETHER_QUARTZ_ORE
    );
    private final AtomicReference<BlockPos> target = new AtomicReference<>();
    private final Deque<BlockPos> queue = new ConcurrentLinkedDeque<>();
    private boolean active = false;

    public void tick(MinecraftClient client) {
        if (!active || client.player == null) return;
        if (!FunTimeMod.humanSimulator.allowMining()) return;
        BlockPos cur = target.get();
        if (cur == null || (MC.world != null && MC.world.getBlockState(cur).isAir())) {
            if (queue.isEmpty()) scanBlocks(client.player.getBlockPos());
            if (!queue.isEmpty()) {
                cur = queue.poll();
                target.set(cur);
                FunTimeMod.LOG.info("Новая цель: {}", cur);
            } else {
                return;
            }
        }
        if (isReachable(cur)) {
            mineBlock(cur);
        } else {
            Pathfinder.walkToAsync(cur, () -> {});
        }
    }

    private void scanBlocks(BlockPos center) {
        if (MC.world == null) return;
        List<BlockPos> found = new ArrayList<>();
        int r = FunTimeMod.CONFIG.minerRadius;
        int bottom = MC.world.getBottomY();
        int top = MC.world.getHeight(); // правильная верхняя граница
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (pos.getY() < bottom || pos.getY() >= top) continue;
                    if (VALUABLE.contains(MC.world.getBlockState(pos).getBlock())) {
                        found.add(pos);
                    }
                }
            }
        }
        found.sort(Comparator.comparingDouble(center::getSquaredDistance));
        queue.clear();
        queue.addAll(found);
    }

    private boolean isReachable(BlockPos pos) {
        Vec3d eye = MC.player.getEyePos();
        Vec3d block = Vec3d.ofCenter(pos);
        return eye.distanceTo(block) <= 4.5;
    }

    private void mineBlock(BlockPos pos) {
        if (MC.interactionManager == null) return;
        MC.interactionManager.attackBlock(pos, Direction.UP);
        if (Math.random() < 0.4) MC.player.swingHand(Hand.MAIN_HAND);
    }

    public void setActive(boolean act) {
        this.active = act;
        if (!act) { target.set(null); queue.clear(); }
    }
}
