package com.axiom.funtime.modules;

import com.axiom.funtime.FunTimeMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
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
    private long lastTargetSwitch = 0;
    public static Entity currentTarget = null;
    public static float serverYaw, serverPitch, realYaw, realPitch;

    public void tick(MinecraftClient client) {
        if (!FunTimeMod.CONFIG.killAuraEnabled || client.player == null || client.world == null) {
            currentTarget = null;
            return;
        }

        // Обновляем реальные повороты для Silent Aim
        realYaw = client.player.getYaw();
        realPitch = client.player.getPitch();

        // Получаем список всех сущностей
        List<Entity> allEntities = new ArrayList<>();
        client.world.getEntities().forEach(allEntities::add);

        // Фильтрация: только живые, не креатив, реальные игроки, не NPC
        List<Entity> validTargets = allEntities.stream()
            .filter(e -> e instanceof LivingEntity && e != client.player)
            .filter(e -> e.isAlive())
            .filter(e -> {
                if (e instanceof PlayerEntity) {
                    // Проверяем, настоящий ли игрок (есть в табе)
                    PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(e.getUuid());
                    if (entry == null) return false; // NPC или бот
                    // Исключаем креатив
                    return !((PlayerEntity) e).isCreative();
                }
                return true; // мобы всегда валидны
            })
            .filter(this::isInReach)
            .sorted(Comparator.comparingDouble(e -> e.distanceTo(client.player)))
            .toList();

        if (validTargets.isEmpty()) {
            currentTarget = null;
            client.options.forwardKey.setPressed(false);
            client.options.sprintKey.setPressed(false);
            return;
        }

        // Выбираем ближайшую цель, но с задержкой переключения для плавности
        Entity target = validTargets.get(0);
        long now = System.currentTimeMillis();
        if (currentTarget != null && !currentTarget.isAlive()) {
            currentTarget = null;
        }
        if (currentTarget == null || (target != currentTarget && now - lastTargetSwitch > 600)) {
            currentTarget = target;
            lastTargetSwitch = now;
        }
        if (currentTarget == null) return;

        // Преследование
        double dist = client.player.distanceTo(currentTarget);
        if (dist > FunTimeMod.CONFIG.killAuraFollowRange) {
            client.options.forwardKey.setPressed(true);
            client.options.sprintKey.setPressed(true);
        } else {
            client.options.forwardKey.setPressed(false);
            client.options.sprintKey.setPressed(false);
        }

        // Рассчитываем нужные повороты на цель
        Vec3d targetPos = getTargetHitboxCenter(currentTarget);
        float[] rotations = calculateRotations(targetPos);

        // Плавное вращение (серверные повороты)
        float maxStep = 60f; // градусов в тик, можно вынести в конфиг
        smoothRotate(rotations, maxStep);
        serverYaw = MC.player.getYaw();
        serverPitch = MC.player.getPitch();
        // Silent Aim уже подменит эти значения при отправке пакета, а камера останется свободной

        // Атака с реалистичными задержками
        if (now - lastAttack >= 450 + random.nextInt(150)) {
            if (random.nextDouble() > FunTimeMod.CONFIG.killAuraMissChance) {
                if (isTargetInHitbox(currentTarget, targetPos)) {
                    MC.interactionManager.attackEntity(MC.player, currentTarget);
                    MC.player.swingHand(Hand.MAIN_HAND);
                }
            } else {
                MC.player.swingHand(Hand.MAIN_HAND); // имитация промаха
            }
            lastAttack = now;
        }
    }

    private boolean isInReach(Entity entity) {
        double hitboxSize = FunTimeMod.CONFIG.killAuraHitboxSize;
        Box box = entity.getBoundingBox().expand(hitboxSize - 1.0);
        Vec3d eye = MC.player.getEyePos();
        Vec3d closest = getClosestPointOnBox(box, eye);
        return eye.squaredDistanceTo(closest) <= 42.25; // 6.5^2
    }

    private Vec3d getClosestPointOnBox(Box box, Vec3d point) {
        double x = Math.max(box.minX, Math.min(point.x, box.maxX));
        double y = Math.max(box.minY, Math.min(point.y, box.maxY));
        double z = Math.max(box.minZ, Math.min(point.z, box.maxZ));
        return new Vec3d(x, y, z);
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

    private void smoothRotate(float[] targetRot, float maxStep) {
        float curYaw = MC.player.getYaw();
        float curPitch = MC.player.getPitch();
        float yawDiff = targetRot[0] - curYaw;
        float pitchDiff = targetRot[1] - curPitch;
        if (yawDiff > 180) yawDiff -= 360;
        if (yawDiff < -180) yawDiff += 360;
        float stepYaw = Math.min(maxStep, Math.abs(yawDiff)) * Math.signum(yawDiff);
        float stepPitch = Math.min(maxStep, Math.abs(pitchDiff)) * Math.signum(pitchDiff);
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
