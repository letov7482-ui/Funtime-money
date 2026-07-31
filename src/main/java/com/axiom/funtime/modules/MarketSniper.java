package com.axiom.funtime.modules;

import net.minecraft.client.MinecraftClient;

public class MarketSniper {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private boolean active = false;

    public void tick(MinecraftClient client) {
        if (!active || client.player == null) return;
        // parse chat for /ah listings, compare prices, snipe if profitable
        // placeholder
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
