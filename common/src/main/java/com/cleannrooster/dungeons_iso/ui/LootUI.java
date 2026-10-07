package com.cleannrooster.dungeons_iso.ui;

import com.cleannrooster.dungeons_iso.api.MinecraftClientAccessor;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LootUI extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        var child = UIContainers.verticalFlow(Sizing.content(),Sizing.content()).alignment(HorizontalAlignment.CENTER,VerticalAlignment.CENTER);
        var components = new ArrayList<ParentUIComponent>();
        components.add((
                UIContainers.horizontalFlow(Sizing.content(),Sizing.content()).child(UIComponents.label(Text.translatable("Nearby Items")))
                        .surface(Surface.panelWithInset(2))
                        .alignment(HorizontalAlignment.CENTER,VerticalAlignment.CENTER)
                        .padding(Insets.of(10))));
        if(MinecraftClient.getInstance().player != null) {
            List<ItemEntity> itemEntityList = MinecraftClient.getInstance().player.getEntityWorld().getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class),MinecraftClient.getInstance().player.getBoundingBox().expand(16),(entity) ->{
                return MinecraftClient.getInstance().player.canSee(entity);
            });
            int ii = 0;
            var vertChildList = UIContainers.horizontalFlow(Sizing.content(),Sizing.content()).alignment(HorizontalAlignment.CENTER,VerticalAlignment.CENTER);
            var horizontalList = new ArrayList<ParentUIComponent>();
            var verticalList = new ArrayList<UIComponent>();
            for(ItemEntity entity : itemEntityList) {
                if(ii < 8) {

                    verticalList.add(
                            UIContainers.horizontalFlow(Sizing.content(),Sizing.content())  .child(UIComponents.button(entity.getName(), button -> {
                                ((MinecraftClientAccessor)MinecraftClient.getInstance()).setLocation(new EntityHitResult(entity,entity.getEntityPos()));
                                ((MinecraftClientAccessor)MinecraftClient.getInstance()).setOriginalLocation(MinecraftClient.getInstance().player.getEntityPos());

                            })).alignment(HorizontalAlignment.CENTER,VerticalAlignment.CENTER));


                    ii++;
                }
                else{
                    ii=0;
                    horizontalList.add(UIContainers.verticalFlow(Sizing.content(),Sizing.content()).children(verticalList).surface(Surface.panelWithInset(2))
                            .alignment(HorizontalAlignment.CENTER,VerticalAlignment.CENTER)
                            .padding(Insets.of(10))); ;
                    verticalList = new ArrayList<UIComponent>();
                }


            }
            horizontalList.add(UIContainers.verticalFlow(Sizing.content(),Sizing.content()).children(verticalList).surface(Surface.panelWithInset(2)).padding(Insets.of(10)).alignment(HorizontalAlignment.CENTER,VerticalAlignment.CENTER)); ;
            ((FlowLayout)vertChildList).children(horizontalList);
            components.add(vertChildList);

        }
        ((FlowLayout)child).children(components);
        rootComponent.child((child));
    }
}
