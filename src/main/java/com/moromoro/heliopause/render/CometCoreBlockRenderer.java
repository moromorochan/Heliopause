package com.moromoro.heliopause.render;

import com.moromoro.heliopause.blockEntity.CometCoreBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Math;

public class CometCoreBlockRenderer extends AbstractCoreBlockRenderer<CometCoreBlockEntity>{
    public CometCoreBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public float getMinOrbSize() {
        return getUVSize() * Math.sqrt(3) * 0.8f;
    }

    @Override
    public float getCoreRotationRatio() {
        return 7.5f;
    }
}
