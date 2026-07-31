package com.axiom.funtime.modules;

import com.axiom.funtime.FunTimeMod;
import com.axiom.funtime.utils.MarketAnalyzer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class AutoTrader {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final Pattern PRICE_PATTERN = Pattern.compile("Цена: (\\d+)");
    private ScheduledExecutorService scheduler;
    private final List<AuctionLot> currentLots = new ArrayList<>();

    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::tick, 5, 15, TimeUnit.SECONDS);
        FunTimeMod.LOG.info("AutoTrader started");
    }

    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        FunTimeMod.LOG.info("AutoTrader stopped");
    }

    public void updateState() {
        if (FunTimeMod.CONFIG.autoTraderEnabled) {
            start();
        } else {
            stop();
        }
    }

    private void tick() {
        if (!FunTimeMod.CONFIG.autoTraderEnabled || MC.player == null) return;
        MC.player.networkHandler.sendChatCommand("ah");
        // Ждём GUI
        try { Thread.sleep(2000); } catch (InterruptedException e) { return; }
        if (MC.currentScreen instanceof HandledScreen<?> screen) {
            parseScreen(screen);
        }
    }

    public void onScreenOpen(HandledScreen<?> screen) {
        if (!FunTimeMod.CONFIG.autoTraderEnabled) return;
        parseScreen(screen);
    }

    private void parseScreen(HandledScreen<?> screen) {
        currentLots.clear();
        for (int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
            ItemStack stack = screen.getScreenHandler().getSlot(i).getStack();
            if (stack.isEmpty()) continue;
            List<Text> lore = stack.getTooltip(Item.TooltipContext.DEFAULT, MC.player, TooltipType.Default.BASIC);
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
        for (AuctionLot lot : currentLots) {
            double fair = MarketAnalyzer.getAveragePrice(lot.item);
            if (lot.price < fair * FunTimeMod.CONFIG.traderProfitThreshold && canAfford(lot.price)) {
                MC.interactionManager.clickSlot(screen.getScreenHandler().syncId, lot.slot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, MC.player);
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                MC.player.networkHandler.sendChatCommand("ah buy");
            }
        }
        // Следующая страница
        int nextPageSlot = screen.getScreenHandler().slots.size() - 1;
        MC.interactionManager.clickSlot(screen.getScreenHandler().syncId, nextPageSlot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, MC.player);
    }

    private boolean canAfford(int price) {
        return true; // TODO: реальный баланс
    }

    public void onChatMessage(Text message) {}

    private static class AuctionLot {
        String item; int price; int slot;
        AuctionLot(String i, int p, int s) { item = i; price = p; slot = s; }
    }
                                                }
