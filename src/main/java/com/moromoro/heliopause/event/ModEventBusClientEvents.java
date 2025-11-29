package com.moromoro.heliopause.event;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.particle.StarRippleParticles;
import com.moromoro.heliopause.particle.WhirlRingParticles;
import com.moromoro.heliopause.particle.FluidSpreadParticles;
import com.moromoro.heliopause.recipe.orreryWhirling.OrreryIngredientLoader;
import com.moromoro.heliopause.registry.*;
import com.moromoro.heliopause.render.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

//クライアント側の挙動を登録するところ
@Mod.EventBusSubscriber(modid = Heliopause.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    //リロード時の処理の登録
    /*@SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event){
        event.addListener(new OrreryIngredientLoader());
    }*/

    //レンダリングタイプの登録
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        //ItemBlockRenderTypes.setRenderLayer(BlockRegistry.MOON.get(), RenderType.translucent());
    }

    //カスタムモデルの登録
    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // 天体
        event.register(new ResourceLocation(Heliopause.MODID, "decoration/phantom/moon"));
        event.register(new ResourceLocation(Heliopause.MODID,"decoration/siderostat_spring"));

        // 陣
        event.register(new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_circle_default"));
        event.register(new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_line_default"));
    }

    //エンティティ・ブロックエンティティレンダラの登録
    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event){
        //ブロックエンティティ
        event.registerBlockEntityRenderer(BlockEntityRegistry.CRUCIBLE_BE.get(), CrucibleBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ORB_BE.get(), OrbBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.FLUID_CAGE_BE.get(), FluidCageBlockRenderer::new);
        //event.registerBlockEntityRenderer(BlockEntityRegistry.FLUID_SPREADER_BE.get(), FluidSpreaderBlockRenderer::new);
        //event.registerBlockEntityRenderer(BlockEntityRegistry.COMET_CORE_BE.get(), CometCoreBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.FLUID_SPREADER_ORB_BE.get(), CentralStarBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.CENTRAL_STAR_BE.get(), CentralStarBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.SIDEROSTAT_BE.get(), SiderostatRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.WRITTEN_BOARD_BE.get(), WrittenBoardRenderer::new);
        //エンティティ
        event.registerEntityRenderer(EntityRegistry.ORRERY_INTERACTION_OPERATOR_E.get(), VoidEntityRenderer::new);
    }

    //パーティクルの登録
    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.FLUID_SPREAD_PARTICLES.get(), FluidSpreadParticles.Provider::new);
        event.registerSpriteSet(ParticleRegistry.WHIRL_RING_PARTICLES.get(), WhirlRingParticles.Provider::new);
        event.registerSpriteSet(ParticleRegistry.STAR_RIPPLE_PARTICLES.get(), StarRippleParticles.Provider::new);
    }
}
