package com.axiom.funtime.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "funtimeubercheat")
public class ModConfig implements ConfigData {
    public boolean autoMinerEnabled = true;
    @ConfigEntry.BoundedDiscrete(min = 8, max = 64)
    public int minerRadius = 32;
    public String priorityOres = "minecraft:ancient_debris,minecraft:diamond_ore,minecraft:emerald_ore";
    public boolean autoSellToBuyer = true;
    public boolean comparePrices = true;
    public int sellReach = 5;
    public boolean sniperEnabled = true;

    // Исправлено: для double используем BoundedFloatingPoint
    @ConfigEntry.BoundedFloatingPoint(min = 0.1, max = 0.9)
    public double sniperProfitThreshold = 0.3;

    public long sniperMaxSpendPerItem = 100_000;
    public boolean inventoryManagerEnabled = true;
    public boolean humanSimulatorEnabled = true;
}
