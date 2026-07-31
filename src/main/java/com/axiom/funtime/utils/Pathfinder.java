package com.axiom.funtime.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Pathfinder {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static CompletableFuture<Void> walkToAsync(BlockPos dest, Runnable onArrival) {
        return CompletableFuture.runAsync(() -> {
            List<BlockPos> path = findPath(MC.player.getBlockPos(), dest, 1000);
            if (path.isEmpty()) return;
            for (BlockPos step : path) {
                moveTo(step);
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
            onArrival.run();
        });
    }

    public static List<BlockPos> findPath(BlockPos start, BlockPos end, int maxNodes) {
        // Простой A* без приколов, идёт по воздуху
        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<BlockPos> closed = new HashSet<>();
        open.add(new Node(start, 0, heuristic(start, end)));
        Map<BlockPos, BlockPos> parent = new HashMap<>();
        while (!open.isEmpty() && closed.size() < maxNodes) {
            Node current = open.poll();
            if (current.pos.equals(end)) {
                List<BlockPos> path = new ArrayList<>();
                BlockPos p = end;
                while (p != null) {
                    path.add(p);
                    p = parent.get(p);
                }
                Collections.reverse(path);
                return path;
            }
            closed.add(current.pos);
            for (BlockPos neighbor : getNeighbors(current.pos)) {
                if (closed.contains(neighbor)) continue;
                double g = current.g + 1;
                double f = g + heuristic(neighbor, end);
                open.add(new Node(neighbor, g, f));
                parent.putIfAbsent(neighbor, current.pos);
            }
        }
        return Collections.emptyList();
    }

    private static List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> list = new ArrayList<>();
        list.add(pos.up());
        list.add(pos.down());
        list.add(pos.north());
        list.add(pos.south());
        list.add(pos.east());
        list.add(pos.west());
        return list;
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private static void moveTo(BlockPos pos) {
        MC.player.updatePosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    private static class Node implements Comparable<Node> {
        BlockPos pos; double g, f;
        Node(BlockPos p, double g, double f) { this.pos = p; this.g = g; this.f = f; }
        public int compareTo(Node o) { return Double.compare(this.f, o.f); }
    }
}
