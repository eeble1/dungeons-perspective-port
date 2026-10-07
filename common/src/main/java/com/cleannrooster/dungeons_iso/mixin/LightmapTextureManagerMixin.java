package com.cleannrooster.dungeons_iso.mixin;

import com.cleannrooster.dungeons_iso.mod.Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LightmapTextureManager.class)
public  class LightmapTextureManagerMixin  {


    // 1.21.11 inlined the old getDarknessFactor(float) as LivingEntity#getEffectFadeFactor.
    @ModifyExpressionValue(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEffectFadeFactor(Lnet/minecraft/registry/entry/RegistryEntry;F)F")
    )
    private float getDarknessFactorFOG(float original) {
        if(Mod.enabled  && MinecraftClient.getInstance().getCameraEntity() != null && MinecraftClient.getInstance().getCameraEntity() instanceof LivingEntity player && player.hasStatusEffect(StatusEffects.DARKNESS)) {
            return 0F;
        }
        return original;
    }
    @Inject(
            method = "getDarkness",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private void getDarknessFOG(LivingEntity entity, float factor, float delta, CallbackInfoReturnable<Float> cir) {
        if(Mod.enabled && MinecraftClient.getInstance().getCameraEntity() != null && MinecraftClient.getInstance().getCameraEntity() instanceof LivingEntity player && player.hasStatusEffect(StatusEffects.DARKNESS) ) {
            cir.setReturnValue(1F);
        }
    }
}
