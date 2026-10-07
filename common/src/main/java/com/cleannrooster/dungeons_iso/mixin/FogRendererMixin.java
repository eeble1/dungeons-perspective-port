package com.cleannrooster.dungeons_iso.mixin;

import com.cleannrooster.dungeons_iso.config.Config;
import com.cleannrooster.dungeons_iso.api.cullers.room.CullingBackdrop;
import com.cleannrooster.dungeons_iso.mod.Mod;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.fog.DarknessEffectFogModifier;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Successor of the 1.21.1 BackgroundRenderer hooks: fog colour, darkness fog and distance fog. */
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @ModifyReturnValue(method = "getFogColor", at = @At("RETURN"))
    private Vector4f shadeCulledCaveBackdrop(Vector4f color) {
        CullingBackdrop.update();
        float amount = CullingBackdrop.strength();
        if (amount <= 0.0F) {
            return color;
        }

        // Preserve the dimension/biome hue, but pull its luminance down toward cave fog. When the
        // sky pass is suppressed this is the colour visible through the intentional cutaway, since
        // the frame is cleared to the fog colour. Fully enclosed settles at true black. During the
        // short transition the original dimension/biome fog colour is retained and smoothly fades out.
        float multiplier = 1.0F - amount;
        color.set(color.x * multiplier, color.y * multiplier, color.z * multiplier, color.w);
        CullingBackdrop.setColor(color.x, color.y, color.z);
        return color;
    }

    /** The darkness effect's fog is suppressed while the perspective is enabled. */
    @WrapOperation(
            method = {"applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;", "getFogColor"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogModifier;shouldApply(Lnet/minecraft/block/enums/CameraSubmersionType;Lnet/minecraft/entity/Entity;)Z")
    )
    private boolean applyFogFogOfWar(FogModifier modifier, CameraSubmersionType submersionType,
                                     Entity cameraEntity, Operation<Boolean> original) {
        if (modifier instanceof DarknessEffectFogModifier && Mod.enabled
                && MinecraftClient.getInstance().getCameraEntity() instanceof LivingEntity living
                && living.hasStatusEffect(StatusEffects.DARKNESS)) {
            return false;
        }
        return original.call(modifier, submersionType, cameraEntity);
    }

    @ModifyArg(
            method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),
            index = 3
    )
    private float distanceFogStart(float environmentalStart) {
        if (Mod.enabled && Config.GSON.instance().distanceFog) {
            return Mod.zoomMetric * (Mod.getZoom() + 1) * 1.5F;
        }
        return environmentalStart;
    }

    @ModifyArg(
            method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),
            index = 4
    )
    private float distanceFogEnd(float environmentalEnd) {
        if (Mod.enabled && Config.GSON.instance().distanceFog) {
            return Mod.zoomMetric * (Mod.getZoom() + 1) * 3F;
        }
        return environmentalEnd;
    }
}
