package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.FluidRegistry;
import com.moromoro.heliopause.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class HeliopauseItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
    public HeliopauseItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Heliopause.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //simpleItem(ItemRegistry.EXAMPLE_ITEM);
        simpleItem(ItemRegistry.ALCHEMY_BIRON_INGOT);
        simpleItem(ItemRegistry.ALCHEMY_BIRON_NUGGET);
        simpleItem(ItemRegistry.GLOWSTONE_ALLOY_INGOT);
        simpleItem(ItemRegistry.GLOWSTONE_ALLOY_NUGGET);
        simpleItem(ItemRegistry.SILVER_INGOT);
        simpleItem(ItemRegistry.SILVER_NUGGET);
        simpleItem(ItemRegistry.RAW_SILVER);
        simpleItem(ItemRegistry.QUINCE_STEEL_INGOT);
        simpleItem(ItemRegistry.CELESTITE);
        simpleItem(ItemRegistry.IMITATION_CORE_ITEM);
        simpleItem(ItemRegistry.OPTICAL_GLASS);
        simpleItem(ItemRegistry.THERMOIMMOBILANT);
        simpleItem(FluidRegistry.STARRY_MIXTURE.bucket());
        simpleItem(FluidRegistry.LIQUEFIED_STARLIGHT.bucket());
        simpleItem(FluidRegistry.LIQUEFIED_TWILIGHT.bucket());
        simpleItem(FluidRegistry.AZURE_STARBEAD.bucket());
        simpleItem(FluidRegistry.SCARLET_STARBEAD.bucket());
        simpleItem(FluidRegistry.SUMMER_STAR_ESSENCE.bucket());
        simpleItem(FluidRegistry.WINTER_STAR_ESSENCE.bucket());
        //simpleItem(ItemRegistry.CHALK_ITEM);
        //simpleItem(ItemRegistry.VIAL_ITEM);
        //simpleItem(ItemRegistry.LARGE_BOTTLE_ITEM);
    }

    private ItemModelBuilder simpleItem(RegistryObject<? extends Item> item){
        return withExistingParent(item.getId().getPath(), new ResourceLocation("item/generated"))
            .texture("layer0", new ResourceLocation(Heliopause.MODID, "item/" + item.getId().getPath()));
    }
}
