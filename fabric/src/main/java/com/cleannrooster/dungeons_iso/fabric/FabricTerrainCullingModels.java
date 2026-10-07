package com.cleannrooster.dungeons_iso.fabric;

import com.cleannrooster.dungeons_iso.api.cullers.room.TerrainCulling;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/** Position-aware model culling through Fabric's public Renderer API. */
public final class FabricTerrainCullingModels {

    private FabricTerrainCullingModels() {
    }

    public static void register() {
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register(
                ModelModifier.WRAP_LAST_PHASE,
                (model, bakeContext) -> {
                    if (model == null || model instanceof CullingModel) {
                        return model;
                    }
                    return new CullingModel(model);
                }));
    }

    private static final class CullingModel extends WrapperBlockStateModel {

        private CullingModel(BlockStateModel wrapped) {
            super(wrapped);
        }

        private static boolean culled(BlockState state, BlockPos pos) {
            try {
                return TerrainCulling.shouldRemove(state, pos.getX(), pos.getY(), pos.getZ());
            } catch (Exception ignored) {
                return false;
            }
        }

        @Override
        public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos,
                              BlockState state, Random random,
                              Predicate<@Nullable Direction> cullTest) {
            if (culled(state, pos)) {
                return;
            }
            super.emitQuads(emitter, blockView, pos, state, random, cullTest);
        }

        @Override
        public @Nullable Object createGeometryKey(BlockRenderView blockView, BlockPos pos,
                                                  BlockState state, Random random) {
            // Culling depends on position, so the geometry may never be shared across positions.
            return null;
        }
    }
}
