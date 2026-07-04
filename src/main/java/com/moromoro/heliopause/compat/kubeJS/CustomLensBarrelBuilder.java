package com.moromoro.heliopause.compat.kubeJS;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.LensBarrelBlock;
import com.moromoro.heliopause.item.LensBarrelBlockItem;
import com.moromoro.heliopause.registry.TagRegistry;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.BlockItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CustomLensBarrelBuilder extends BlockBuilder {
    private final String part;
    
    public CustomLensBarrelBuilder(ResourceLocation id, String part) {
        super(id);
        this.part = part;
        
        this.itemBuilder = new CustomLensBarrelItemBuilder(this);
    }
    
    @Override
    public Block createObject() {
        return new LensBarrelBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1.0F));
    }
    
    @Override
    public void createAdditionalObjects() {
        super.createAdditionalObjects();
        
        // タグ付与
        this.tag(TagRegistry.Blocks.LENS_BARREL.location());
        switch (part) {
            case "main" -> this.tag(TagRegistry.Blocks.MAIN_MIRROR.location());
            case "second" -> this.tag(TagRegistry.Blocks.SECOND_MIRROR.location());
            default -> Heliopause.LOGGER.error("{} is unknown part of lensBarrel.(barrel/main/second)",part);
        }
    }
    
    public static class CustomLensBarrelItemBuilder extends BlockItemBuilder {
        private final CustomLensBarrelBuilder builder;
        public CustomLensBarrelItemBuilder(CustomLensBarrelBuilder builder) {
            super(builder.id);
            this.builder = builder;
        }
        
        @Override
        public Item createObject() {
            Block block = builder.get();
            return new LensBarrelBlockItem(block, new Item.Properties());
        }
    }
}
