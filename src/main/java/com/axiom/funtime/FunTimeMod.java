package com.axiom.funtime;

import com.axiom.funtime.config.ModConfig;
import com.axiom.funtime.modules.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
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

    @Override
    public void onInitialize() {
        LOG.info("FunTime UberCheat starting...");
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        humanSimulator = new HumanSimulator();
        humanSimulator.start();

        autoMiner = new AutoMiner();
        invManager = new InventoryManager();
        sniper = new MarketSniper();
        antiCheat = new AntiCheatEvasion();

        // Инициализируем автотрейдер
        autoTrader = new AutoTrader();
        if (CONFIG.autoTraderEnabled) {
            autoTrader.start();
        }

        // Клавиша для AutoMiner
        KeyBinding toggleMinerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.funtimeubercheat.autominer", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M,
                "category.funtimeubercheat"));

        // Тик игрового клиента
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleMinerKey.wasPressed()) {
                CONFIG.autoMinerEnabled = !CONFIG.autoMinerEnabled;
                AutoConfig.getConfigHolder(ModConfig.class).save();
                LOG.info("AutoMiner: {}", CONFIG.autoMinerEnabled ? "ON" : "OFF");
            }
            if (client.player == null) return;
            antiCheat.onTick();
            if (CONFIG.autoMinerEnabled) autoMiner.tick(client);
            if (CONFIG.inventoryManagerEnabled) invManager.tick(client);
            if (CONFIG.sniperEnabled) sniper.tick(client);
        });

        // Обработка чата (для трейдера)
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage) -> {
            if (CONFIG.autoTraderEnabled && autoTrader != null) {
                autoTrader.onChatMessage(message);
            }
        });
    }
}
