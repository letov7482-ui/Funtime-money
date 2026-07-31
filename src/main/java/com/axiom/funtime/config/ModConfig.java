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

    // Новые поля для автотрейдера
    public boolean autoTraderEnabled = true;
    public double traderProfitThreshold = 0.3;   // насколько % дешевле рынка брать
    public long traderMaxSpendPerItem = 100_000;  // макс цена покупки
}
