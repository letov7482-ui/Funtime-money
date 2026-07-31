package com.axiom.funtime.mixin;

import com.axiom.funtime.FunTimeMod;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (FunTimeMod.CONFIG.autoTraderEnabled && FunTimeMod.autoTrader != null) {
            FunTimeMod.autoTrader.onScreenOpen((HandledScreen<?>) (Object) this);
        }
    }
}
