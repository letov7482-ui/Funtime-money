package com.axiom.funtime.utils;

import net.minecraft.util.math.BlockPos;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Pathfinder {
    public static List<BlockPos> findPath(BlockPos start, BlockPos end, int maxNodes) {
        // A* implementation placeholder
        return Collections.singletonList(end);
    }

    public static CompletableFuture<Void> walkToAsync(BlockPos destination, Runnable onArrival) {
        return CompletableFuture.runAsync(() -> {
            // execute movement in separate thread
            onArrival.run();
        });
    }
}
