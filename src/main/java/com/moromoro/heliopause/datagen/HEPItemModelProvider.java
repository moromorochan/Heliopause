package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class HEPItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
    public HEPItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Heliopause.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //simpleItem(ItemRegistry.EXAMPLE_ITEM);
        simpleItem(ItemRegistry.ALCHEMY_BIRON_INGOT);
        simpleItem(ItemRegistry.ALCHEMY_BIRON_NUGGET);
        simpleItem(ItemRegistry.GLOWSTONE_ALLOY_INGOT);
        simpleItem(ItemRegistry.GLOWSTONE_ALLOY_NUGGET);
        simpleItem(ItemRegistry.CELESTITE);
        //simpleItem(ItemRegistry.CHALK_ITEM);
        //simpleItem(ItemRegistry.VIAL_ITEM);
        //simpleItem(ItemRegistry.LARGE_BOTTLE_ITEM);
    }

    private ItemModelBuilder simpleItem(RegistryObject<? extends Item> item){
        return withExistingParent(item.getId().getPath(), new ResourceLocation("item/generated"))
            .texture("layer0", new ResourceLocation(Heliopause.MODID, "item/" + item.getId().getPath()));
    }
}
