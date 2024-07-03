package com.moromoro.heliopause.render;

import com.moromoro.heliopause.blockEntity.OrbBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class OrbBlockRenderer extends AbstractFluidOrbBlockRenderer<OrbBlockEntity> {
    public OrbBlockRenderer(BlockEntityRendererProvider.Context context){
        super(context);
    }
}
