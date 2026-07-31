package com.axiom.funtime.mixin;

import com.axiom.funtime.FunTimeMod;
import com.axiom.funtime.modules.KillAura;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class SilentAimMixin {
    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void onSendMovementPackets(CallbackInfo ci) {
        if (FunTimeMod.CONFIG.killAuraEnabled && KillAura.currentTarget != null) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            float originalYaw = player.getYaw();
            float originalPitch = player.getPitch();
            player.setYaw(KillAura.serverYaw);
            player.setPitch(KillAura.serverPitch);
            player.setYaw(originalYaw);  // восстанавливаем сразу, чтобы камера не дёргалась
            player.setPitch(originalPitch);
        }
    }
}
