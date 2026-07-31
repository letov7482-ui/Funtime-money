package com.axiom.funtime.modules;

import com.axiom.funtime.FunTimeMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import java.util.*;
import java.util.stream.Collectors;

public class KillAura {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private long lastAttack = 0;

    public void tick(MinecraftClient client) {
        if (!FunTimeMod.CONFIG.killAuraEnabled || client.player == null) return;
        List<Entity> entityList = new ArrayList<>();
        client.world.getEntities().forEach(entityList::add);
        Entity target = entityList.stream()
            .filter(e -> e instanceof LivingEntity && e != client.player)
            .filter(e -> !(e instanceof PlayerEntity) || !((PlayerEntity) e).isCreative())
            .filter(e -> e.distanceTo(client.player) < 6.0)
            .min(Comparator.comparingDouble(e -> e.distanceTo(client.player)))
            .orElse(null);
        if (target == null) return;
        long now = System.currentTimeMillis();
        if (now - lastAttack < 400) return;
        Vec3d diff = target.getPos().add(0, target.getHeight() / 2, 0).subtract(client.player.getEyePos());
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
        client.player.setYaw(yaw);
        client.player.setPitch(pitch);
        MC.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
    }
}
