package com.moromoro;

import com.mojang.logging.LogUtils;
import com.moromoro.heliopause.event.TooltipEventHandler;
import com.moromoro.heliopause.item.FluidBottle;
import com.moromoro.heliopause.registry.*;
import com.moromoro.heliopause.screen.ConcentratorScreen;
import com.moromoro.heliopause.screen.RoastingTableScreen;
import com.moromoro.heliopause.screen.SiderostatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static com.moromoro.heliopause.registry.BlockEntityRegistry.BLOCKENTITIES;
import static com.moromoro.heliopause.registry.BlockRegistry.BLOCKS;
import static com.moromoro.heliopause.registry.CreativeTabRegistry.CREATIVE_MODE_TABS;
import static com.moromoro.heliopause.registry.EntityRegistry.ENTITIES;
import static com.moromoro.heliopause.registry.FluidRegistry.FLUIDS;
import static com.moromoro.heliopause.registry.FluidRegistry.FLUID_TYPES;
import static com.moromoro.heliopause.registry.ItemRegistry.ITEMS;
import static com.moromoro.heliopause.registry.MenuTypeRegistry.MENU_TYPES;
import static com.moromoro.heliopause.registry.ParticleRegistry.PARTICLE_TYPES;
import static com.moromoro.heliopause.registry.RecipeSerializerRegistry.RECIPE_SERIALIZERS;
import static com.moromoro.heliopause.registry.RecipeTypeRegistry.RECIPE_TYPES;
import static com.moromoro.heliopause.registry.SoundRegistry.SOUNDS;

@Mod(Heliopause.MODID)
public class Heliopause {

    // 参照するmodIDを定義
    public static final String MODID = "heliopause";
    //public static final String MODNAME = "Heliopause";

    // slf4j logger を参照
    public static final Logger LOGGER = LogUtils.getLogger();

    //ブロック・ブロックエンティティ・アイテムの登録

    public Heliopause()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Deferred Register を MOD イベント バスに登録して、ブロック・アイテム・クリエイティブタブが登録されるように
        BLOCKS.register(modEventBus);
        BLOCKENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        FLUIDS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        ENTITIES.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        SOUNDS.register(modEventBus);

        //configファイルの登録
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHolder.SPEC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //ブロックへのホバーでツールチップを表示する
        MinecraftForge.EVENT_BUS.register(new TooltipEventHandler());
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

            //メニューとスクリーンを紐づけ
            MenuScreens.register(MenuTypeRegistry.ROASTING_TABLE_MENU.get(), RoastingTableScreen::new);
            MenuScreens.register(MenuTypeRegistry.SIDEROSTAT_MENU.get(), SiderostatScreen::new);
            MenuScreens.register(MenuTypeRegistry.CONCENTRATOR_MENU.get(), ConcentratorScreen::new);

            //キーコンフィグの追加

            // 模造天体のデータ登録
            /*Minecraft.getInstance().execute(() -> {
                if(Minecraft.getInstance().getResourceManager() instanceof ReloadableResourceManager manager){
                    manager.registerReloadListener(
                        new ImitationCoreAssemblyRecipe(new Gson(), "imitation_core")
                    );
                }
            });*/

            // アイテムの色を登録
            registerItemColors(ItemRegistry.VIAL_ITEM.get(),1);
            registerItemColors(ItemRegistry.LARGE_BOTTLE_ITEM.get(),1);
        }

        private static void registerItemColors(ItemLike itemLike,int targetIndex) {
            // MinecraftのインスタンスからItemColorsを取得
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
