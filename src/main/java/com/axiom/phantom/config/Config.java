package com.axiom.phantom.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "phantom")
public class Config implements ConfigData {
    // Общие
    public boolean autoSprintEnabled = true;
    public boolean humanSimulatorEnabled = true;
    public boolean autoTraderEnabled = false;
    public boolean killAuraEnabled = false;
    public boolean autoMinerEnabled = false;
    public boolean inventoryManagerEnabled = false;
    public boolean eventHelperEnabled = false;

    // KillAura
    public double killAuraHitbox = 1.2;
    public double killAuraMissChance = 0.1;
    public double killAuraFollowRange = 3.5;
    public boolean killAuraAntiNPC = true;
}
