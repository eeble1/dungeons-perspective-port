package com.cleannrooster.dungeons_iso.neoforge.client;

import com.cleannrooster.dungeons_iso.api.cullers.room.TerrainCulling;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import java.util.List;

/** Position-aware model culling through NeoForge's level-aware block model API. */
@EventBusSubscriber(modid = "dungeons_iso", value = Dist.CLIENT)
public final class NeoForgeTerrainCullingModels {

    private NeoForgeTerrainCullingModels() {
    }

    @SubscribeEvent
    public static void modifyModels(ModelEvent.ModifyBakingResult event) {
        event.getBakingResult().blockStateModels().replaceAll((state, model) ->
                model instanceof CullingModel ? model : new CullingModel(model));
    }

    private static final class CullingModel extends DelegateBlockStateModel {

        private CullingModel(BlockStateModel originalModel) {
            super(originalModel);
        }

        @Override
        public void collectParts(BlockRenderView level, BlockPos pos, BlockState state,
                                 Random random, List<BlockModelPart> parts) {
            try {
                if (TerrainCulling.shouldRemove(state, pos.getX(), pos.getY(), pos.getZ())) {
                    return;
                }
            } catch (Exception ignored) {
            }
            super.collectParts(level, pos, state, random, parts);
        }
    }
}
