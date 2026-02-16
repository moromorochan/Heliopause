package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.item.ImitationCoreItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

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

                // 鏡筒
                output.accept(BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get());
                output.accept(BlockRegistry.WOODEN_MAIN_MIRROR_BLOCK.get());
                output.accept(BlockRegistry.WOODEN_SECOND_MIRROR_BLOCK.get());

                // 素材
                output.accept(BlockRegistry.ALCHEMY_BIRON_BLOCK.get());
                output.accept(ItemRegistry.ALCHEMY_BIRON_INGOT.get());
                output.accept(ItemRegistry.ALCHEMY_BIRON_NUGGET.get());
                output.accept(BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get());
                output.accept(ItemRegistry.GLOWSTONE_ALLOY_INGOT.get());
                output.accept(ItemRegistry.GLOWSTONE_ALLOY_NUGGET.get());
                output.accept(ItemRegistry.CELESTITE.get());
                output.accept(ItemRegistry.OPTICAL_GLASS.get());

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
            })
            .build());
}
