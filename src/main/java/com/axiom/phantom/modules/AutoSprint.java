package com.axiom.phantom.modules;

import net.minecraft.client.MinecraftClient;

public class AutoSprint {
    public static void tick(MinecraftClient client) {
        if (client.player != null && client.player.isOnGround() && client.options.forwardKey.isPressed()) {
            client.player.setSprinting(true);
        }
    }
}
