package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;
import java.util.function.Consumer;

public class FluidRegistry {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Heliopause.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Heliopause.MODID);

    // レコードクラス
        public record FluidEntry(RegistryObject<Fluid> still, RegistryObject<Fluid> flowing,
                                 RegistryObject<LiquidBlock> block, RegistryObject<LiquidBlock> flowBlock, RegistryObject<Item> bucket) {}

    public static FluidEntry registerFluidPair(String name, BlockBehaviour.Properties blockProperties, FluidType.Properties fluidProperties) {
        @SuppressWarnings("unchecked")
        final RegistryObject<Fluid>[] fluids = new RegistryObject[2];
        final RegistryObject<LiquidBlock>[] blocks = new RegistryObject[2];
        // バケツアイテム
        final RegistryObject<Item> bucket = ItemRegistry.ITEMS.register(name + "_bucket",
            () -> new BucketItem(fluids[0], new Item.Properties()
                .stacksTo(1)
                .craftRemainder(Items.BUCKET)
            ));

        // fluidType
        final RegistryObject<FluidType> type = FLUID_TYPES.register(name,
            () -> new FluidType(fluidProperties){
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private final ResourceLocation STILL = new ResourceLocation(Heliopause.MODID, "block/fluid/" + name + "_still");
                        private final ResourceLocation FLOW = new ResourceLocation(Heliopause.MODID, "block/fluid/" + name + "_flow");
                        private final ResourceLocation OVERLAY = new ResourceLocation("minecraft", "misc/underwater");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLOW;
                        }

                        @Override
                        public ResourceLocation getOverlayTexture() {
                            return OVERLAY;
                        }

                        @Override
                        public int getTintColor() {
                            return FastColor.ARGB32.color(255,255,255,255);
                        }
                    });

                }
            }
        );

        // stillFluid
        fluids[0] = FLUIDS.register(name, () -> new ForgeFlowingFluid.Source(
            new ForgeFlowingFluid.Properties(type, () -> fluids[0].get(), () -> fluids[1].get()
            ).bucket(bucket).block(blocks[0])
        ));
        // flowingFluid
        fluids[1] = FLUIDS.register("flowing_" + name, () -> new ForgeFlowingFluid.Flowing(
            new ForgeFlowingFluid.Properties(type, () -> fluids[0].get(), () -> fluids[1].get()
            ).bucket(bucket).block(blocks[1])
        ));

        blocks[0] = BlockRegistry.BLOCKS.register(name,
            ()-> new LiquidBlock(() -> (FlowingFluid) fluids[0].get(), blockProperties.noLootTable()));
        blocks[1] = BlockRegistry.BLOCKS.register("flowing_" + name,
            () -> new LiquidBlock(() -> (FlowingFluid) fluids[1].get(), blockProperties.noLootTable()));

        return new FluidEntry(fluids[0], fluids[1], blocks[0], blocks[1], bucket);
    }

    public static final FluidEntry STARRY_MIXTURE =
        registerFluidPair("starry_mixture",
            BlockBehaviour.Properties.copy(Blocks.WATER).lightLevel((blockState)-> 7),
            FluidType.Properties.create().density(1000).viscosity(100).lightLevel(7));

    public static final FluidEntry LIQUEFIED_STARLIGHT =
        registerFluidPair("liquefied_starlight",BlockBehaviour.Properties.copy(Blocks.WATER).lightLevel((blockState)-> 15),
            FluidType.Properties.create().density(100).viscosity(100).lightLevel(15));

    public static final FluidEntry LIQUEFIED_TWILIGHT =
        registerFluidPair("liquefied_twilight",BlockBehaviour.Properties.copy(Blocks.WATER),
            FluidType.Properties.create().density(1000).viscosity(1000));

    public static final FluidEntry AZURE_STARBEAD =
        registerFluidPair("azure_starbead",BlockBehaviour.Properties.copy(Blocks.WATER).lightLevel((blockState)-> 7),
            FluidType.Properties.create().density(1000).viscosity(1000).lightLevel(7));

    public static final FluidEntry SCARLET_STARBEAD =
        registerFluidPair("scarlet_starbead",BlockBehaviour.Properties.copy(Blocks.WATER).lightLevel((blockState)-> 7),
            FluidType.Properties.create().density(1000).viscosity(1000).lightLevel(7));

    public static final FluidEntry SUMMER_STAR_ESSENCE =
        registerFluidPair("summer_star_essence",BlockBehaviour.Properties.copy(Blocks.WATER),
            FluidType.Properties.create().density(1000).viscosity(1000).lightLevel(15));

    public static final FluidEntry WINTER_STAR_ESSENCE =
        registerFluidPair("winter_star_essence",BlockBehaviour.Properties.copy(Blocks.WATER),
            FluidType.Properties.create().density(1000).viscosity(1000).lightLevel(15));

}
