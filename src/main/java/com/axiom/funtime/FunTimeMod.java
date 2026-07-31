package com.axiom.funtime;

import com.axiom.funtime.config.ModConfig;
import com.axiom.funtime.modules.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunTimeMod implements ModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("FunTimeUber");
    public static ModConfig CONFIG;
    public static HumanSimulator humanSimulator;
    public static AutoTrader autoTrader;
    private AutoMiner autoMiner;
    private InventoryManager invManager;
    private MarketSniper sniper;
    private AntiCheatEvasion antiCheat;
    private KillAura killAura;
    private KeyBinding killAuraKey;

    @Override
    public void onInitialize() {
        LOG.info("FunTime UberCheat starting...");
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        humanSimulator = new HumanSimulator();
        if (CONFIG.humanSimulatorEnabled) humanSimulator.start();

        autoMiner = new AutoMiner();
        invManager = new InventoryManager();
        sniper = new MarketSniper();
        antiCheat = new AntiCheatEvasion();
        killAura = new KillAura();

        autoTrader = new AutoTrader();
        if (CONFIG.autoTraderEnabled) autoTrader.start();

        // Бинд для AutoMiner
        KeyBinding toggleMinerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.funtimeubercheat.autominer", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M,
                "category.funtimeubercheat"));
        // Бинд для KillAura
        killAuraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.funtimeubercheat.killaura", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G,
                "category.funtimeubercheat"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Обработка клавиш
            while (toggleMinerKey.wasPressed()) {
                CONFIG.autoMinerEnabled = !CONFIG.autoMinerEnabled;
                AutoConfig.getConfigHolder(ModConfig.class).save();
                LOG.info("AutoMiner: {}", CONFIG.autoMinerEnabled ? "ON" : "OFF");
            }
            while (killAuraKey.wasPressed()) {
                CONFIG.killAuraEnabled = !CONFIG.killAuraEnabled;
                AutoConfig.getConfigHolder(ModConfig.class).save();
                LOG.info("KillAura: {}", CONFIG.killAuraEnabled ? "ON" : "OFF");
            }
            if (client.player == null) return;
            antiCheat.onTick();
            autoTrader.updateState();
            if (CONFIG.autoMinerEnabled) autoMiner.tick(client);
            if (CONFIG.inventoryManagerEnabled) invManager.tick(client);
            if (CONFIG.sniperEnabled) sniper.tick(client);
            if (CONFIG.killAuraEnabled) killAura.tick(client);
        });

        LOG.info("FunTime UberCheat ready. Бинды: M - AutoMiner, G - KillAura");
    }
}
