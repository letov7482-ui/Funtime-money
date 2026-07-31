package com.axiom.funtime.modules;

import com.axiom.funtime.FunTimeMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.*;

public class KillAura {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private final Random random = new Random();
    private long lastAttack = 0;

    public void tick(MinecraftClient client) {
        if (!FunTimeMod.CONFIG.killAuraEnabled || client.player == null || client.world == null) return;

        List<Entity> allEntities = new ArrayList<>();
        client.world.getEntities().forEach(allEntities::add);

        Entity target = allEntities.stream()
            .filter(e -> e instanceof LivingEntity && e != client.player)
            .filter(e -> !(e instanceof PlayerEntity) || !((PlayerEntity) e).isCreative())
            .filter(e -> e.isAlive())
            .filter(this::isInReach)
            .min(Comparator.comparingDouble(e -> e.distanceTo(client.player)))
            .orElse(null);

        if (target == null) {
            client.options.forwardKey.setPressed(false);
            client.options.sprintKey.setPressed(false);
            return;
        }

        double dist = client.player.distanceTo(target);
        if (dist > FunTimeMod.CONFIG.killAuraFollowRange) {
            client.options.forwardKey.setPressed(true);
            client.options.sprintKey.setPressed(true);
        } else {
            client.options.forwardKey.setPressed(false);
            client.options.sprintKey.setPressed(false);
        }

        Vec3d targetPos = getTargetHitboxCenter(target);
        float[] rotations = calculateRotations(targetPos);

        if (FunTimeMod.CONFIG.killAuraSnap) {
            client.player.setYaw(rotations[0]);
            client.player.setPitch(rotations[1]);
        } else {
            smoothRotate(rotations, 30f);
        }

        long now = System.currentTimeMillis();
        if (now - lastAttack >= 400 + random.nextInt(100)) {
            if (random.nextDouble() > FunTimeMod.CONFIG.killAuraMissChance) {
                if (isTargetInHitbox(target, targetPos)) {
                    MC.interactionManager.attackEntity(client.player, target);
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            } else {
                client.player.swingHand(Hand.MAIN_HAND);
            }
            lastAttack = now;
        }
    }

    private boolean isInReach(Entity entity) {
        double hitboxSize = FunTimeMod.CONFIG.killAuraHitboxSize;
        Box box = entity.getBoundingBox().expand(hitboxSize - 1.0);
        Vec3d eye = MC.player.getEyePos();
        Vec3d closest = getClosestPointOnBox(box, eye);
        return eye.squaredDistanceTo(closest) <= 36.0;
    }

    // Ручная реализация ближайшей точки на боксе
    private Vec3d getClosestPointOnBox(Box box, Vec3d point) {
        double x = clamp(point.x, box.minX, box.maxX);
        double y = clamp(point.y, box.minY, box.maxY);
        double z = clamp(point.z, box.minZ, box.maxZ);
        return new Vec3d(x, y, z);
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    private Vec3d getTargetHitboxCenter(Entity target) {
        return target.getPos().add(0, target.getHeight() * 0.85, 0);
    }

    private float[] calculateRotations(Vec3d targetPos) {
        Vec3d eye = MC.player.getEyePos();
        Vec3d diff = targetPos.subtract(eye);
        double hDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, hDist));
        return new float[]{yaw, pitch};
    }

    private void smoothRotate(float[] targetRot, float maxAngleStep) {
        float curYaw = MC.player.getYaw();
        float curPitch = MC.player.getPitch();
        float yawDiff = targetRot[0] - curYaw;
        float pitchDiff = targetRot[1] - curPitch;
        if (yawDiff > 180) yawDiff -= 360;
        if (yawDiff < -180) yawDiff += 360;
        float stepYaw = Math.min(maxAngleStep, Math.abs(yawDiff)) * Math.signum(yawDiff);
        float stepPitch = Math.min(maxAngleStep, Math.abs(pitchDiff)) * Math.signum(pitchDiff);
        MC.player.setYaw(curYaw + stepYaw);
        MC.player.setPitch(curPitch + stepPitch);
    }

    private boolean isTargetInHitbox(Entity target, Vec3d targetPos) {
        double hitboxSize = FunTimeMod.CONFIG.killAuraHitboxSize;
        Vec3d eye = MC.player.getEyePos();
        Vec3d direction = targetPos.subtract(eye).normalize();
        double distance = eye.distanceTo(targetPos) + hitboxSize;
        Vec3d end = eye.add(direction.multiply(distance));
        Box box = target.getBoundingBox().expand(hitboxSize - 1.0);
        return box.raycast(eye, end).isPresent();
    }
}
