package com.axiom.funtime.modules;

import com.axiom.funtime.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

public class EventHelper {
    private static final Map<String, BlockPos> waypoints = new ConcurrentHashMap<>();
    private static final Pattern COORD = Pattern.compile("(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)");

    public static void onChatMessage(String msg) {
        // Примеры: "Мистик найден на 100 64 200", "Эвент начнётся на ..."
        String lower = msg.toLowerCase();
        Matcher m = COORD.matcher(msg);
        if (m.find()) {
            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            int z = Integer.parseInt(m.group(3));
            BlockPos pos = new BlockPos(x, y, z);
            if (lower.contains("мистик")) waypoints.put("М", pos);
            else if (lower.contains("эвент")) waypoints.put("Э", pos);
            else if (lower.contains("вулкан")) waypoints.put("В", pos);
        }
    }

    public static void render() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        for (Map.Entry<String, BlockPos> e : waypoints.entrySet()) {
            BlockPos pos = e.getValue();
            double dist = mc.player.getPos().distanceTo(pos.toCenterPos());
            String text = e.getKey() + " " + String.format("%.1fм", dist);
            RenderUtils.drawLabel(pos, text, 0xFFFFFF00); // жёлтый цвет, смотри RenderUtils
        }
    }
    }
