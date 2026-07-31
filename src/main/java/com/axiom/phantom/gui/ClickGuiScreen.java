package com.axiom.phantom.gui;

import com.axiom.phantom.gui.components.CategoryButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private final List<CategoryButton> categories = new ArrayList<>();
    private int mouseX, mouseY;

    public ClickGuiScreen() {
        super(Text.literal("Phantom GUI"));
        categories.add(new CategoryButton(10, 10, "Combat"));
        categories.add(new CategoryButton(80, 10, "Movement"));
        categories.add(new CategoryButton(150, 10, "World"));
        categories.add(new CategoryButton(220, 10, "Misc"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        // Полупрозрачный фон
        context.fill(0, 0, width, height, 0x80000000);
        for (CategoryButton cat : categories) {
            cat.render(context, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryButton cat : categories) {
            if (cat.mouseClicked((int) mouseX, (int) mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
