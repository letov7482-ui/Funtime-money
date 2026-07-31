package com.axiom.funtime.modules;

import com.axiom.funtime.FunTimeMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class AutoTrader {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final Pattern AH_LISTING = Pattern.compile(
        "\\[AH] #(\\d+) ([^ ]+) (\\d+) монет", Pattern.CASE_INSENSITIVE);
    private static final Pattern BUY_CONFIRM = Pattern.compile(
        "Вы купили (.+) за (\\d+) монет", Pattern.CASE_INSENSITIVE);

    private final ConcurrentHashMap<String, List<Integer>> priceHistory = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> fairPrices = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean enabled = false;

    public void start() {
        if (enabled) return;
        enabled = true;
        scheduler.scheduleAtFixedRate(this::scanAndTrade, 10, 30, TimeUnit.SECONDS);
        FunTimeMod.LOG.info("AutoTrader запущен");
    }

    public void stop() {
        enabled = false;
        scheduler.shutdownNow();
    }

    public void onChatMessage(Text message) {
        if (!enabled || message == null) return;
        String msg = message.getString();

        // Парсим листинги
        Matcher m = AH_LISTING.matcher(msg);
        while (m.find()) {
            int lotId = Integer.parseInt(m.group(1));
            String item = m.group(2);
            int price = Integer.parseInt(m.group(3));
            addPriceRecord(item, price);
            if (isUndervalued(item, price)) {
                buyLot(lotId);
            }
        }

        // Покупка подтверждена — перевыставляем
        Matcher buy = BUY_CONFIRM.matcher(msg);
        if (buy.find()) {
            String item = buy.group(1);
            int paid = Integer.parseInt(buy.group(2));
            resellItem(item, paid);
        }
    }

    private void addPriceRecord(String item, int price) {
        priceHistory.computeIfAbsent(item, k -> new ArrayList<>()).add(price);
        if (priceHistory.get(item).size() % 10 == 0) {
            recomputeFairPrice(item);
        }
    }

    private void recomputeFairPrice(String item) {
        List<Integer> prices = priceHistory.get(item);
        if (prices.isEmpty()) return;
        List<Integer> sorted = new ArrayList<>(prices);
        Collections.sort(sorted);
        int cut = (int) (sorted.size() * 0.2);
        List<Integer> mid = sorted.subList(cut, sorted.size() - cut);
        double avg = mid.stream().mapToInt(Integer::intValue).average().orElse(0);
        fairPrices.put(item, avg);
    }

    private boolean isUndervalued(String item, int price) {
        Double fair = fairPrices.get(item);
        if (fair == null || fair <= 0) return false;
        double threshold = FunTimeMod.CONFIG.traderProfitThreshold;
        long maxSpend = FunTimeMod.CONFIG.traderMaxSpendPerItem;
        return price < fair * (1.0 - threshold) && price <= maxSpend;
    }

    private void buyLot(int lotId) {
        if (MC.player == null) return;
        FunTimeMod.LOG.info("Выкупаем лот #{}", lotId);
        MC.player.networkHandler.sendChatCommand("ah buy " + lotId);
        try { Thread.sleep(1500 + ThreadLocalRandom.current().nextInt(1000)); } catch (InterruptedException ignored) {}
    }

    private void resellItem(String item, int paid) {
        if (MC.player == null) return;
        Double fair = fairPrices.get(item);
        if (fair == null || fair <= 0) fair = paid * 1.4;
        long sellPrice = (long) (fair * 0.95);
        MC.player.networkHandler.sendChatCommand("ah sell " + sellPrice);
        FunTimeMod.LOG.info("Выставили {} за {} монет", item, sellPrice);
    }

    private void scanAndTrade() {
        if (MC.player == null) return;
        MC.player.networkHandler.sendChatCommand("ah");
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
          }
