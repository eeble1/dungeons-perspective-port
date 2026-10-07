package com.cleannrooster.dungeons_iso.mixin;

import com.cleannrooster.dungeons_iso.ClientInit;
import com.cleannrooster.dungeons_iso.config.Config;
import com.cleannrooster.dungeons_iso.api.cullers.room.GhostRenderer;
import com.cleannrooster.dungeons_iso.api.cullers.room.CullingBackdrop;
import com.cleannrooster.dungeons_iso.mod.Mod;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws a distinct outline around the contextual interactable target ({@link Mod#targetedInteractable}),
 * and the ghost of culled terrain.
 *
 * Piggybacks on vanilla's main world pass: injected where gizmos are collected inside the main
 * frame-graph pass, after opaque terrain, entities and block entities have been submitted and
 * while the entity {@link VertexConsumerProvider.Immediate} is live and not yet flushed. Like
 * entities, geometry is submitted camera-relative with an identity {@link MatrixStack}; the
 * camera rotation is already on the model-view stack. The gold outline is visibly different
 * from vanilla's block outline, so the contextual target is easy to tell apart from the raw
 * mouse target. It appears only while Dungeons Perspective is enabled and vanishes the instant
 * the target becomes invalid.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void hideSkyBehindCulledCaves(FrameGraphBuilder frameGraphBuilder, Camera camera,
                                          GpuBufferSlice fogBuffer, CallbackInfo ci) {
        if (CullingBackdrop.hidesSky()) {
            // The frame's clear pass runs before the sky pass and fills the colour buffer with the
            // fog colour, which FogRendererMixin has already pulled toward the cave backdrop.
            // Skipping the sky leaves exactly that colour behind the cutaway.
            ci.cancel();
        }
    }

    @Inject(method = "collectGizmos", at = @At("HEAD"))
    private void drawWorldOverlaysXIV(CallbackInfo ci) {
        Camera camera = this.client.gameRenderer.getCamera();
        MatrixStack matrices = new MatrixStack();
        VertexConsumerProvider.Immediate buffers = this.bufferBuilders.getEntityVertexConsumers();
        try {
            GhostRenderer.render(matrices, buffers, camera, this.client.gameRenderer);
        } catch (Exception ignored) {
        }
        if (!Mod.enabled || !Config.GSON.instance().isContextualInteract()) {
            return;
        }
        BlockPos pos = Mod.targetedInteractable;
        if (pos == null) {
            return;
        }
        MinecraftClient client = this.client;
        if (client.world == null || client.player == null) {
            return;
        }
        World world = client.world;
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        VoxelShape shape = state.getOutlineShape(world, pos, ShapeContext.of(camera.getFocusedEntity()));
        if (shape.isEmpty()) {
            return;
        }
        Vec3d cam = camera.getCameraPos();
        VertexConsumer lines = buffers.getBuffer(RenderLayers.lines());
        // Gold, higher alpha than vanilla's outline → clearly distinct from the mouse target.
        ClientInit.drawCuboidShapeOutline(matrices, lines, shape,
                pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z,
                1.0F, 0.82F, 0.15F, 0.9F);
        buffers.drawCurrentLayer();
    }
}
