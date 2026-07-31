package com.axiom.funtime.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import java.util.Comparator;

public class KillAura {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private boolean enabled = false;
    private long lastAttack = 0;

    public void tick(MinecraftClient client) {
        if (!enabled || client.player == null) return;
        // ищем ближайшую цель в радиусе 6
        Entity target = client.world.getEntities().stream()
            .filter(e -> e instanceof LivingEntity && e != client.player)
            .filter(e -> !(e instanceof PlayerEntity) || !((PlayerEntity) e).isCreative())
            .filter(e -> e.distanceTo(client.player) < 6.0)
            .min(Comparator.comparingDouble(e -> e.distanceTo(client.player)))
            .orElse(null);
        if (target == null) return;
        long now = System.currentTimeMillis();
        if (now - lastAttack < 400) return; // задержка как у игрока
        // поворачиваем голову
        Vec3d diff = target.getPos().add(0, target.getHeight() / 2, 0).subtract(client.player.getEyePos());
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
        client.player.setYaw(yaw);
        client.player.setPitch(pitch);
        // атакуем
        MC.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
    }

    public void setEnabled(boolean en) { this.enabled = en; }
}
