package com.axiom.funtime.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "funtimeubercheat")
public class ModConfig implements ConfigData {
    public boolean autoMinerEnabled = true;
    public int minerRadius = 32;
    public String priorityOres = "minecraft:ancient_debris,minecraft:diamond_ore,minecraft:emerald_ore";
    public boolean autoSellToBuyer = true;
    public boolean comparePrices = true;
    public int sellReach = 5;
    public boolean sniperEnabled = true;
    public double sniperProfitThreshold = 0.3;
    public long sniperMaxSpendPerItem = 100_000;
    public boolean inventoryManagerEnabled = true;
    public boolean humanSimulatorEnabled = true;
    public boolean autoTraderEnabled = true;
    public double traderProfitThreshold = 0.3;
    public long traderMaxSpendPerItem = 100_000;
    public boolean killAuraEnabled = true;
    public boolean eventHelperEnabled = true;

    // KillAura settings
    public double killAuraHitboxSize = 2.0;         // множитель хитбокса (1.0 = стандартный)
    public boolean killAuraSnap = true;             // мгновенный поворот к цели
    public double killAuraMissChance = 0.15;        // шанс промаха (0.0 - 0.5)
    public double killAuraFollowRange = 4.0;        // дистанция для преследования
    public int killAuraKeybind = 71;                // код клавиши (по умолч. G)
}
