package com.axiom.funtime.modules;

import com.axiom.funtime.FunTimeMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class AutoTrader {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final Pattern PRICE_PATTERN = Pattern.compile("Цена: (\\d+)");
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<AuctionLot> currentLots = new ArrayList<>();
    private boolean enabled = false;

    public void start() {
        if (enabled) return;
        enabled = true;
        scheduler.scheduleAtFixedRate(this::tick, 5, 15, TimeUnit.SECONDS);
    }

    public void stop() { enabled = false; scheduler.shutdownNow(); }

    private void tick() {
        if (MC.player == null) return;
        // открываем аукцион
        MC.player.networkHandler.sendChatCommand("ah");
        // ждём открытия GUI (асинхронно)
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        if (MC.currentScreen instanceof HandledScreen<?> screen) {
            parseScreen(screen);
        }
    }

    public void onScreenOpen(HandledScreen<?> screen) {
        if (!enabled) return;
        parseScreen(screen);
    }

    private void parseScreen(HandledScreen<?> screen) {
        currentLots.clear();
        for (int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
            ItemStack stack = screen.getScreenHandler().getSlot(i).getStack();
            if (!stack.isEmpty()) {
                // читаем название и цену из lore (упрощённо, ищем строку с ценой)
                List<Text> lore = stack.getTooltip();
                int price = -1;
                for (Text line : lore) {
                    Matcher m = PRICE_PATTERN.matcher(line.getString());
                    if (m.find()) {
                        price = Integer.parseInt(m.group(1));
                        break;
                    }
                }
                if (price > 0) {
                    currentLots.add(new AuctionLot(stack.getItem().getName().getString(), price, i));
                }
            }
        }
        // ищем выгодные
        for (AuctionLot lot : currentLots) {
            double fair = MarketAnalyzer.getAveragePrice(lot.item);
            if (lot.price < fair * 0.7 && canAfford(lot.price)) {
                // кликаем по слоту
                MC.interactionManager.clickSlot(screen.getScreenHandler().syncId, lot.slot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, MC.player);
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                // подтверждение покупки (зависит от сервера, часто просто второй клик или команда /ah buy)
                MC.player.networkHandler.sendChatCommand("ah buy");
            }
        }
        // кнопка следующей страницы (если есть)
        // предположим, последний слот — "Next Page"
        int nextPageSlot = screen.getScreenHandler().slots.size() - 1;
        MC.interactionManager.clickSlot(screen.getScreenHandler().syncId, nextPageSlot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, MC.player);
    }

    private boolean canAfford(int price) {
        // проверяем баланс через чат или счётчик (заглушка)
        return true;
    }

    public void onChatMessage(Text message) {
        // старый парсинг на случай, если GUI не открылся
    }

    private static class AuctionLot {
        String item; int price; int slot;
        AuctionLot(String i, int p, int s) { item = i; price = p; slot = s; }
    }
}
