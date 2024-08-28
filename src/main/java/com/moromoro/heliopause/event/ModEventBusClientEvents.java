package com.moromoro.heliopause.event;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.particle.FluidSpreadParticles;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import com.moromoro.heliopause.render.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//クライアント側の挙動を登録するところ
@Mod.EventBusSubscriber(modid = Heliopause.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {

    //ブロックエンティティレンダラの登録
    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(BlockEntityRegistry.CRUCIBLE_BE.get(), CrucibleBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ORB_BE.get(), OrbBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.FLUID_CAGE_BE.get(), FluidCageBlockRenderer::new);
        //event.registerBlockEntityRenderer(BlockEntityRegistry.FLUID_SPREADER_BE.get(), FluidSpreaderBlockRenderer::new);
        //event.registerBlockEntityRenderer(BlockEntityRegistry.COMET_CORE_BE.get(), CometCoreBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.FLUID_SPREADER_ORB_BE.get(), FluidSpreaderOrbBlockRenderer::new);
    }

    //パーティクルの登録
    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.FLUID_SPREAD_PARTICLES.get(), FluidSpreadParticles.Provider::new);
    }
}
