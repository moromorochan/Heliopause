package com.moromoro;

import com.mojang.logging.LogUtils;
import com.moromoro.heliopause.item.FluidBottle;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(Heliopause.MODID)
public class Heliopause {

    // 参照するmodIDを定義
    public static final String MODID = "heliopause";

    // slf4j logger を参照
    private static final Logger LOGGER = LogUtils.getLogger();

    //ブロック・ブロックエンティティ・アイテムの登録

    // mod名前空間に登録されるブロックを保持するための遅延レジスタを作成
    public static final DeferredRegister<Block> BLOCKS = BlockRegistry.BLOCKS;

    // mod名前空間に登録されるブロックエンティティを保持するための遅延レジスタを作成
    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITIES = BlockEntityRegistry.BLOCKENTITIES;

    // mod名前空間に登録されるアイテムを保持するための遅延レジスタを作成
    public static final DeferredRegister<Item> ITEMS = ItemRegistry.ITEMS;

    // mod名前空間に登録されるクリエイティブモードタブを保持するための遅延レジスタを作成
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // aquosolis:example_tab クリエイティブタブを作成
    public static final RegistryObject<CreativeModeTab> HELIOPAUSE_TAB_MAIN = CREATIVE_MODE_TABS.register("heliopause_main", () -> CreativeModeTab.builder()
            //.withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.heliopause_main"))
            .icon(() -> ItemRegistry.ALCHEMY_REACTOR_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.ALCHEMY_REACTOR_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(ItemRegistry.CRUCIBLE_ITEM.get());
                output.accept(ItemRegistry.ALCHEMY_BIRON_BLOCK_ITEM.get());
                output.accept(ItemRegistry.ALCHEMY_BIRON_INGOT_ITEM.get());
                output.accept(ItemRegistry.ALCHEMY_BIRON_NUGGET_ITEM.get());
                output.accept(ItemRegistry.GLOWSTONE_ALLOY_BLOCK_ITEM.get());
                output.accept(ItemRegistry.GLOWSTONE_ALLOY_INGOT_ITEM.get());
                output.accept(ItemRegistry.GLOWSTONE_ALLOY_NUGGET_ITEM.get());
                output.accept(ItemRegistry.VIAL_ITEM.get());
                output.accept(ItemRegistry.LARGE_BOTTLE_ITEM.get());
            }).build());

    public Heliopause()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Deferred Register を MOD イベント バスに登録して、ブロック・アイテム・クリエイティブタブが登録されるように
        BLOCKS.register(modEventBus);
        BLOCKENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        //configファイルの登録
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHolder.SPEC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    // SubscribeEvent を使用することで、イベントバスが呼び出すメソッドを検出できるようになる
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

            // アイテムの色を登録
            registerItemColors(ItemRegistry.VIAL_ITEM.get(),1);
            registerItemColors(ItemRegistry.LARGE_BOTTLE_ITEM.get(),1);
        }

        private static void registerItemColors(ItemLike itemLike,int targetIndex) {
            // MinecraftのItemColorsインスタンスを取得
            ItemColors itemColors = Minecraft.getInstance().getItemColors();

            // アイテムと色を関連させる
            itemColors.register((stack, tintIndex) -> {
                //対象のレイヤーに色を適用
                if(tintIndex == targetIndex)
                {
                    // NBTデータから色情報を取得します。
                    if (stack.hasTag() && stack.getTag().contains(FluidBottle.COLOR_NBT_KEY)) {
                        return stack.getTag().getInt("color");
                    }
                    // nbt色データが無かった場合、デフォルトの色(マゼンタ)を返す
                    return 0xFF00FF;
                }
                //対象でないレイヤーには白を返す
                return 0xFFFFFF;

            }, itemLike);
        }
    }

}
