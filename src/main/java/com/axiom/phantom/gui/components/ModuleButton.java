package com.axiom.phantom.gui.components;

import com.axiom.phantom.PhantomClient;
import com.axiom.phantom.config.Config;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ModuleButton {
    public int x, y, width = 55, height = 15;
    public String name, configKey;

    public ModuleButton(int x, int y, String name, String configKey) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.configKey = configKey;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        boolean enabled = getConfigValue();
        int color = enabled ? 0xFF00AA00 : 0xFFAA0000;
        context.fill(x, y, x + width, y + height, color);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, name, x + 2, y + 2, 0xFFFFFF);
    }

    public boolean mouseClicked(int mx, int my, int button) {
        if (mx >= x && mx <= x + width && my >= y && my <= y + height) {
            if (button == 0) { // ЛКМ – вкл/выкл
                toggle();
                return true;
            } else if (button == 1) { // ПКМ – настройки (заглушка)
                // Откроем окно настроек, но пока просто выведем в лог
                PhantomClient.LOG.info("Настройки модуля {} не реализованы", name);
                return true;
            }
        }
        return false;
    }

    private boolean getConfigValue() {
        try {
            var field = Config.class.getField(configKey);
            return field.getBoolean(PhantomClient.CONFIG);
        } catch (Exception e) {
            return false;
        }
    }

    private void toggle() {
        try {
            var field = Config.class.getField(configKey);
            field.setBoolean(PhantomClient.CONFIG, !field.getBoolean(PhantomClient.CONFIG));
            AutoConfig.getConfigHolder(Config.class).save();
        } catch (Exception e) {}
    }
}
