package com.axiom.phantom.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class CategoryButton {
    public int x, y, width = 60, height = 20;
    public String name;
    public boolean expanded;
    private final List<ModuleButton> modules = new ArrayList<>();

    public CategoryButton(int x, int y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
        // Пример наполнения модулями
        if (name.equals("Movement")) {
            modules.add(new ModuleButton(x, y + 25, "AutoSprint", "autoSprintEnabled"));
            modules.add(new ModuleButton(x, y + 50, "AutoMiner", "autoMinerEnabled"));
        }
        // Добавь другие категории по аналогии
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        int color = expanded ? 0xFF777777 : 0xFF555555;
        context.fill(x, y, x + width, y + height, color);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, name, x + 5, y + 5, -1);
        if (expanded) {
            for (ModuleButton mod : modules) {
                mod.render(context, mouseX, mouseY);
            }
        }
    }

    public boolean mouseClicked(int mx, int my, int button) {
        if (mx >= x && mx <= x + width && my >= y && my <= y + height) {
            expanded = !expanded;
            return true;
        }
        if (expanded) {
            for (ModuleButton mod : modules) {
                if (mod.mouseClicked(mx, my, button)) return true;
            }
        }
        return false;
    }
}
