// путь: src/main/java/com/axiom/funtime/mixin/ChatMessageMixin.java
package com.axiom.funtime.mixin;

import com.axiom.funtime.FunTimeMod;
import com.axiom.funtime.modules.EventHelper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatMessageMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        // Передаём сообщение в AutoTrader
        if (FunTimeMod.CONFIG.autoTraderEnabled && FunTimeMod.autoTrader != null) {
            FunTimeMod.autoTrader.onChatMessage(packet.content());
        }
        // Передаём сообщение в EventHelper для парсинга координат событий
        if (FunTimeMod.CONFIG.eventHelperEnabled) {
            EventHelper.onChatMessage(packet.content().getString());
        }
    }
}
