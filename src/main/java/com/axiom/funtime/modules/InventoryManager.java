package com.axiom.funtime.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import java.util.Set;

public class InventoryManager {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final Set<net.minecraft.item.Item> JUNK = Set.of(
            Items.COBBLESTONE, Items.DIRT, Items.GRAVEL
    );
    private long lastClean = 0;

    public void tick(MinecraftClient client) {
        if (client.player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastClean < 5000) return;
        lastClean = now;

        if (isInventoryFull(client)) {
            sellAndClean(client);
        } else {
            dropJunk(client);
        }
    }

    private boolean isInventoryFull(MinecraftClient client) {
        return client.player.getInventory().getEmptySlot() == -1;
    }

    private void sellAndClean(MinecraftClient client) {
        // execute /buyer command via chat
        client.player.networkHandler.sendChatCommand("buyer");
        // drop junk
        dropJunk(client);
    }

    private void dropJunk(MinecraftClient client) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (!stack.isEmpty() && JUNK.contains(stack.getItem())) {
                MC.interactionManager.clickSlot(0, i, 1, SlotActionType.THROW, client.player);
            }
        }
    }
}
