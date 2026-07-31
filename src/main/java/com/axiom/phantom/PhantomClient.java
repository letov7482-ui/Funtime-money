package com.axiom.phantom;

import com.axiom.phantom.config.Config;
import com.axiom.phantom.gui.ClickGuiScreen;
import com.axiom.phantom.modules.*;
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

public class PhantomClient implements ModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("Phantom");
    public static Config CONFIG;
    public static HumanSimulator humanSimulator;
    public static AutoTrader autoTrader;
    public static BehaviorRecorder behaviorRecorder;
    public static KillAura killAura;

    @Override
    public void onInitialize() {
        LOG.info("Phantom Client starting...");
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(Config.class).getConfig();

        humanSimulator = new HumanSimulator();
        autoTrader = new AutoTrader();
        behaviorRecorder = new BehaviorRecorder();
        killAura = new KillAura();

        // Бинд на открытие GUI (RSHIFT)
        KeyBinding guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.phantom.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.phantom"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (guiKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }
            if (client.player == null) return;
            // Постоянно работающие модули
            if (CONFIG.autoSprintEnabled) AutoSprint.tick(client);
            if (CONFIG.humanSimulatorEnabled) humanSimulator.tick(client);
            if (CONFIG.autoTraderEnabled) autoTrader.tick(client);
            if (CONFIG.killAuraEnabled && KillAura.profile != null) killAura.tick(client);
            // другие модули вызываются аналогично, сейчас для краткости опущены
        });

        LOG.info("Phantom Client ready. RSHIFT для GUI.");
    }
}
