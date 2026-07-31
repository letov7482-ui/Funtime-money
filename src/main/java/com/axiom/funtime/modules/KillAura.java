package com.axiom.funtime.modules;

import com.axiom.funtime.FunTimeMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import java.util.*;

public class KillAura {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private final Random random = new Random();
    private long lastAttack = 0;
    private long lastRotationUpdate = 0;

    public void tick(MinecraftClient client) {
        if (!FunTimeMod.CONFIG.killAuraEnabled || client.player == null || client.world == null) return;

        List<Entity> allEntities = new ArrayList<>();
        client.world.getEntities().forEach(allEntities::add);

        // Фильтруем цели
        Entity target = allEntities.stream()
            .filter(e -> e instanceof LivingEntity && e != client.player)
            .filter(e -> !(e instanceof PlayerEntity) || !((PlayerEntity) e).isCreative())
            .filter(e -> e.isAlive())
            .filter(e -> isInReach(e)) // проверка расстояния с учётом хитбокса
            .min(Comparator.comparingDouble(e -> e.distanceTo(client.player)))
            .orElse(null);

        if (target == null) {
            // Отпускаем клавиши бега, если никого нет
            client.options.forwardKey.setPressed(false);
            return;
        }

        // Преследование
        double dist = client.player.distanceTo(target);
        if (dist > FunTimeMod.CONFIG.killAuraFollowRange) {
            // Бежим к цели
            client.options.forwardKey.setPressed(true);
            client.options.sprintKey.setPressed(true); // автобег
        } else {
            client.options.forwardKey.setPressed(false);
            client.options.sprintKey.setPressed(false);
        }

        // Aim с плавностью или snap
        Vec3d targetPos = getTargetHitboxCenter(target);
        float[] rotations = calculateRotations(targetPos);

        if (FunTimeMod.CONFIG.killAuraSnap) {
            // Мгновенный поворот
            client.player.setYaw(rotations[0]);
            client.player.setPitch(rotations[1]);
        } else {
            // Плавное вращение
            smoothRotate(rotations, 30f); // 30 градусов в тик
        }

        // Атака с задержкой и миссами
        long now = System.currentTimeMillis();
        if (now - lastAttack >= 400 + random.nextInt(100)) { // разброс задержки
            // Проверка на мисс
            if (random.nextDouble() > FunTimeMod.CONFIG.killAuraMissChance) {
                // Радиусная проверка попадания с хитбоксом
                if (isTargetInHitbox(target, targetPos)) {
                    MC.interactionManager.attackEntity(client.player, target);
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            } else {
                // Промах: просто свинг
                client.player.swingHand(Hand.MAIN_HAND);
            }
            lastAttack = now;
        }
    }

    private boolean isInReach(Entity entity) {
        double hitboxSize = FunTimeMod.CONFIG.killAuraHitboxSize;
        // Увеличиваем bounding box цели
        Box box = entity.getBoundingBox().expand(hitboxSize - 1.0);
        Vec3d eye = MC.player.getEyePos();
        // Простой чек: расстояние от глаз до ближайшей точки расширенного бокса <= 6.0
        return eye.squaredDistanceTo(box.getClosestPoint(eye)) <= 36.0; // 6^2
    }

    private Vec3d getTargetHitboxCenter(Entity target) {
        // Центр обычного хитбокса + смещение на высоту глаз
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
        float currentYaw = MC.player.getYaw();
        float currentPitch = MC.player.getPitch();

        // Нормализуем разницу
        float yawDiff = targetRot[0] - currentYaw;
        float pitchDiff = targetRot[1] - currentPitch;

        if (yawDiff > 180) yawDiff -= 360;
        if (yawDiff < -180) yawDiff += 360;

        float stepYaw = Math.min(maxAngleStep, Math.abs(yawDiff)) * Math.signum(yawDiff);
        float stepPitch = Math.min(maxAngleStep, Math.abs(pitchDiff)) * Math.signum(pitchDiff);

        MC.player.setYaw(currentYaw + stepYaw);
        MC.player.setPitch(currentPitch + stepPitch);
    }

    private boolean isTargetInHitbox(Entity target, Vec3d targetPos) {
        double hitboxSize = FunTimeMod.CONFIG.killAuraHitboxSize;
        // Делаем рейкаст от глаз игрока к расширенной области
        Vec3d eye = MC.player.getEyePos();
        Vec3d direction = targetPos.subtract(eye).normalize();
        double distance = eye.distanceTo(targetPos) + hitboxSize;
        Vec3d end = eye.add(direction.multiply(distance));

        // Проверяем пересечение луча с увеличенным боксом цели
        Box box = target.getBoundingBox().expand(hitboxSize - 1.0);
        return box.raycast(eye, end).isPresent();
    }
                                 }
