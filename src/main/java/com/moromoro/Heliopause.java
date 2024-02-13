package com.moromoro;

import com.moromoro.heliopause.block.ReactorBlock;
import com.mojang.logging.LogUtils;
import com.moromoro.heliopause.block.CrucibleBlock;
import com.moromoro.heliopause.blockEntity.CrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Heliopause.MODID)
public class Heliopause {

    // 参照するmodIDを定義
    public static final String MODID = "heliopause";

    // slf4j logger を参照
    private static final Logger LOGGER = LogUtils.getLogger();

    // mod名前空間に登録されるブロックを保持するための遅延レジスタを作成
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    // mod名前空間に登録されるアイテムを保持するための遅延レジスタを作成
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // mod名前空間に登録されるクリエイティブモードタブを保持するための遅延レジスタを作成
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // ブロックの作成
    //錬金こん炉
    public static final RegistryObject<Block> ALCHEMY_REACTOR =
            BLOCKS.register("alchemy_reactor", () -> new ReactorBlock());
    public static final RegistryObject<Item> ALCHEMY_REACTOR_ITEM =
            ITEMS.register("alchemy_reactor", () -> new BlockItem(ALCHEMY_REACTOR.get(), new Item.Properties()));

    //るつぼ
    public static final RegistryObject<Block> CRUCIBLE =
            BLOCKS.register("crucible", () -> new CrucibleBlock(
                    BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.NETHERITE_BLOCK)
                    )
            );
    public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE_BE =
            BLOCKENTITIES.register("crucible",() ->
                    BlockEntityType.Builder.of(CrucibleBlockEntity::new,
                            CRUCIBLE.get()).build(null)
                    );
    public static final RegistryObject<Item> CRUCIBLE_ITEM =
            ITEMS.register("crucible", () -> new BlockItem(CRUCIBLE.get(), new Item.Properties()));

    //錬金赤銅ブロック
    public static final RegistryObject<Block> ALCHEMY_BIRON_BLOCK =
            BLOCKS.register("alchemy_biron_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.NETHERITE_BLOCK)
                    )
            );
    public static final RegistryObject<Item> ALCHEMY_BIRON_BLOCK_ITEM =
            ITEMS.register("alchemy_biron_block", () -> new BlockItem(ALCHEMY_BIRON_BLOCK.get(), new Item.Properties()));

    //錬金赤銅インゴット
    public static final RegistryObject<Item> ALCHEMY_BIRON_INGOT_ITEM =
            ITEMS.register("alchemy_biron_ingot", () -> new Item(new Item.Properties()));
    //錬金赤銅ナゲット
    public static final RegistryObject<Item> ALCHEMY_BIRON_NUGGET_ITEM =
            ITEMS.register("alchemy_biron_nugget", () -> new Item(new Item.Properties()));

    //グロウストーン合金ブロック
    public static final RegistryObject<Block> GLOWSTONE_ALLOY_BLOCK =
            BLOCKS.register("glowstone_alloy_block", () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .strength(2.0F)
                                    .sound(SoundType.COPPER)
                                    .lightLevel(state -> 9)
                    )
            );
    public static final RegistryObject<Item> GLOWSTONE_ALLOY_BLOCK_ITEM =
            ITEMS.register("glowstone_alloy_block", () -> new BlockItem(GLOWSTONE_ALLOY_BLOCK.get(), new Item.Properties()));

    //グロウストーン合金インゴット
    public static final RegistryObject<Item> GLOWSTONE_ALLOY_INGOT_ITEM =
            ITEMS.register("glowstone_alloy_ingot", () -> new Item(new Item.Properties()));
    //グロウストーン合金ナゲット
    public static final RegistryObject<Item> GLOWSTONE_ALLOY_NUGGET_ITEM =
            ITEMS.register("glowstone_alloy_nugget", () -> new Item(new Item.Properties()));
    // aquosolis:example_tab クリエイティブタブを作成
    public static final RegistryObject<CreativeModeTab> HELIOPAUSE_TAB_MAIN = CREATIVE_MODE_TABS.register("heliopause_main", () -> CreativeModeTab.builder()
            //.withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.heliopause_main"))
            .icon(() -> ALCHEMY_REACTOR_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ALCHEMY_REACTOR_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(CRUCIBLE_ITEM.get());
                output.accept(ALCHEMY_BIRON_BLOCK_ITEM.get());
                output.accept(ALCHEMY_BIRON_INGOT_ITEM.get());
                output.accept(ALCHEMY_BIRON_NUGGET_ITEM.get());
                output.accept(GLOWSTONE_ALLOY_BLOCK_ITEM.get());
                output.accept(GLOWSTONE_ALLOY_INGOT_ITEM.get());
                output.accept(GLOWSTONE_ALLOY_NUGGET_ITEM.get());
            }).build());

    public Heliopause()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Deferred Register を MOD イベント バスに登録して、ブロック・アイテム・クリエイティブタブが登録されるように
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCKENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }


    //Add the example block item to the building blocks tab
    /*private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }
    */

    // SubscribeEvent を使用することで、イベント バスが呼び出すメソッドを検出できるようになる
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // EventBusSubscriber を使用すると、@SubscribeEvent アノテーションが付けられたクラス内のすべての静的メソッドを自動的に登録できる
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
