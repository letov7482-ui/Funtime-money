package com.axiom.funtime.modules;

import com.axiom.funtime.FunTimeMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HumanSimulator {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final Random RAND = new Random();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean enabled = false;
    private volatile int idleState = 0; // 0-активен, 1-AFK, 2-альтернативное занятие

    public void start() {
        if (enabled) return;
        enabled = true;
        scheduleNextRandomAction();
        FunTimeMod.LOG.info("HumanSimulator enabled");
    }

    public void stop() {
        enabled = false;
        scheduler.shutdownNow();
    }

    private void scheduleNextRandomAction() {
        if (!enabled) return;
        int delay = 5 + RAND.nextInt(40);
        scheduler.schedule(this::performRandomInterruption, delay, TimeUnit.SECONDS);
    }

    private void performRandomInterruption() {
        if (!enabled || MC.player == null) return;
        int choice = RAND.nextInt(100);
        try {
            if (choice < 25) {
                goTemporaryAfk(10 + RAND.nextInt(20));
            } else if (choice < 50) {
                fakeActivity("fishing");
            } else if (choice < 75) {
                sendRandomChatMessage();
            } else {
                wanderAround(10 + RAND.nextInt(20));
            }
        } catch (Exception e) {
            // ignore
        } finally {
            scheduleNextRandomAction();
        }
    }

    private void goTemporaryAfk(int seconds) {
        ClientPlayerEntity player = MC.player;
        long start = System.currentTimeMillis();
        idleState = 1;
        while (System.currentTimeMillis() - start < seconds * 1000L && enabled) {
            if (RAND.nextFloat() < 0.3) {
                float yaw = player.getYaw() + (RAND.nextFloat() * 40 - 20);
                float pitch = player.getPitch() + (RAND.nextFloat() * 10 - 5);
                pitch = Math.max(-90, Math.min(90, pitch));
                player.setYaw(yaw);
                player.setPitch(pitch);
            }
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
        idleState = 0;
    }

    private void fakeActivity(String type) {
        idleState = 2;
        if (type.equals("fishing") && MC.player.getInventory().getMainHandStack().getItem() == net.minecraft.item.Items.FISHING_ROD) {
            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
        }
        try { Thread.sleep(2000 + RAND.nextInt(5000)); } catch (InterruptedException ignored) {}
        idleState = 0;
    }

    private void wanderAround(int seconds) {
        ClientPlayerEntity player = MC.player;
        long start = System.currentTimeMillis();
        idleState = 2;
        while (System.currentTimeMillis() - start < seconds * 1000L && enabled) {
            if (RAND.nextFloat() < 0.4) {
                MC.options.forwardKey.setPressed(RAND.nextBoolean());
                MC.options.leftKey.setPressed(RAND.nextBoolean());
                MC.options.backKey.setPressed(RAND.nextBoolean());
                MC.options.rightKey.setPressed(RAND.nextBoolean());
            }
            try { Thread.sleep(400 + RAND.nextInt(800)); } catch (InterruptedException ignored) {}
            MC.options.forwardKey.setPressed(false);
            MC.options.leftKey.setPressed(false);
            MC.options.backKey.setPressed(false);
            MC.options.rightKey.setPressed(false);
        }
        idleState = 0;
    }

    private void sendRandomChatMessage() {
        if (MC.player == null) return;
        String[] phrases = {
            "го на арену", "кто продаст алмазы?", "лаги блин", "фармить пойду",
            "никто не знает почём нынче изумруды?", "апнул 30 лвл", "скучно"
        };
        String msg = phrases[RAND.nextInt(phrases.length)];
        MC.player.networkHandler.sendChatMessage(msg);
    }

    public boolean allowMining() {
        return enabled && idleState == 0;
    }
        }
