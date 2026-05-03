package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.item.ImitationCoreItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Heliopause.MODID);

    public static final RegistryObject<CreativeModeTab> HELIOPAUSE_TAB_MAIN = CREATIVE_MODE_TABS.register("heliopause_main",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(BlockRegistry.ALCHEMY_STOVE.get()))
            .title(Component.translatable("creativetab.heliopause_tab"))
            .displayItems((parameters, output) -> {
                // 機構
                //output.accept(BlockRegistry.ALCHEMY_CAMPFIRE.get());
                //output.accept(BlockRegistry.ALCHEMY_STOVE.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                //output.accept(BlockRegistry.ROASTING_TABLE.get());
                //output.accept(BlockRegistry.FLUID_CAGE.get());
                //output.accept(BlockRegistry.FLUID_SPREADER_TOWER.get());
                //output.accept(ItemRegistry.LOW_COPPER_PIPE_ITEM.get());
                //output.accept(ItemRegistry.MEDIUM_COPPER_PIPE_ITEM.get());
               // output.accept(ItemRegistry.HIGH_COPPER_PIPE_ITEM.get());
                //output.accept(BlockRegistry.SIDEROSTAT_BASE.get());
                output.accept(BlockRegistry.SIDEROSTAT_TOP.get());
                //output.accept(BlockRegistry.PENETRATOR.get());
                //output.accept(BlockRegistry.DISSOLVER.get());
                //output.accept(BlockRegistry.CONVERGE_CYLINDER.get());
                //output.accept(BlockRegistry.REFINERY_CYLINDER.get());
                output.accept(BlockRegistry.BLACKBOARD.get());
                output.accept(BlockRegistry.CRUCIBLE.get());
                //output.accept(ItemRegistry.COMET_CORE_ITEM.get());
                output.accept(BlockRegistry.CONCENTRATOR.get());
                //output.accept(BlockRegistry.INGREDIENT_DISPENSER.get());
                //output.accept(BlockRegistry.INGREDIENT_COLLECTOR.get());

                // 鏡筒
                output.accept(BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get());
                output.accept(BlockRegistry.STONE_LENS_BARREL_BLOCK.get());
                output.accept(BlockRegistry.ALCHEMY_BIRON_LENS_BARREL_BLOCK.get());
                output.accept(BlockRegistry.THERMOIMMOBILANT_LENS_BARREL_BLOCK.get());
                output.accept(BlockRegistry.IRON_MAIN_MIRROR_BLOCK.get());
                output.accept(BlockRegistry.IRON_SECOND_MIRROR_BLOCK.get());
                output.accept(BlockRegistry.GRAPHITE_MAIN_MIRROR_BLOCK.get());
                output.accept(BlockRegistry.GRAPHITE_SECOND_MIRROR_BLOCK.get());
                output.accept(BlockRegistry.SILVER_MAIN_MIRROR_BLOCK.get());
                output.accept(BlockRegistry.SILVER_SECOND_MIRROR_BLOCK.get());
                output.accept(BlockRegistry.QUINCE_STEEL_MAIN_MIRROR_BLOCK.get());
                output.accept(BlockRegistry.QUINCE_STEEL_SECOND_MIRROR_BLOCK.get());

                // 素材
                output.accept(BlockRegistry.ALCHEMY_BIRON_BLOCK.get());
                output.accept(ItemRegistry.ALCHEMY_BIRON_INGOT.get());
                output.accept(ItemRegistry.ALCHEMY_BIRON_NUGGET.get());
                output.accept(BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get());
                output.accept(ItemRegistry.GLOWSTONE_ALLOY_INGOT.get());
                output.accept(ItemRegistry.GLOWSTONE_ALLOY_NUGGET.get());
                output.accept(BlockRegistry.SILVER_BLOCK.get());
                output.accept(BlockRegistry.RAW_SILVER_BLOCK.get());
                output.accept(BlockRegistry.SILVER_ORE_BLOCK.get());
                output.accept(BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get());
                output.accept(ItemRegistry.SILVER_INGOT.get());
                output.accept(ItemRegistry.SILVER_NUGGET.get());
                output.accept(ItemRegistry.RAW_SILVER.get());
                output.accept(ItemRegistry.QUINCE_STEEL_INGOT.get());
                output.accept(ItemRegistry.CELESTITE.get());
                output.accept(ItemRegistry.OPTICAL_GLASS.get());
                output.accept(ItemRegistry.THERMOIMMOBILANT.get());
                output.accept(ItemRegistry.GRAVITY_COIL.get());

                //液体
                output.accept(FluidRegistry.STARRY_MIXTURE.bucket().get());
                output.accept(FluidRegistry.LIQUEFIED_STARLIGHT.bucket().get());
                output.accept(FluidRegistry.LIQUEFIED_TWILIGHT.bucket().get());
                output.accept(FluidRegistry.AZURE_STARBEAD.bucket().get());
                output.accept(FluidRegistry.SCARLET_STARBEAD.bucket().get());
                output.accept(FluidRegistry.SUMMER_STAR_ESSENCE.bucket().get());
                output.accept(FluidRegistry.WINTER_STAR_ESSENCE.bucket().get());

                // ツール
                output.accept(ItemRegistry.COMPASS_ITEM.get());
                //output.accept(ItemRegistry.IMITATION_CORE_ITEM.get());
                //output.accept(ItemRegistry.CHALK_ITEM.get());
                //output.accept(ItemRegistry.RULER_ITEM.get());
                //output.accept(ItemRegistry.VIAL_ITEM.get());
                //output.accept(ItemRegistry.LARGE_BOTTLE_ITEM.get());
                //output.accept(BlockRegistry.PAPERBUSH_BLOCK.get());
                //output.accept(BlockRegistry.PAPERBUSH_LEAVES_BLOCK.get());
                //output.accept(ItemRegistry.PAPERBUSH_TWIGS_ITEM.get());

                // その他
                output.accept(ItemRegistry.IMITATION_CORE_ITEM.get());
                output.accept(ImitationCoreItem.getImitationCoreWithTag("satellite"));/*,"rocky", 0.8f, 255,255,255, 0.5f*/
                output.accept(ImitationCoreItem.getImitationCoreWithTag("crimson_planet"));
                output.accept(ImitationCoreItem.getImitationCoreWithTag("indigo_marbling"));
                output.accept(ImitationCoreItem.getImitationCoreWithTag("ancient_star"));
                
                // 建材
                output.accept(BlockRegistry.POLISHED_BIRON_BLOCK.get());
                output.accept(BlockRegistry.CHISELED_BIRON_BLOCK.get());
                output.accept(BlockRegistry.ALCHEMY_BIRON_GLASS.get());
            })
            .build());
}
