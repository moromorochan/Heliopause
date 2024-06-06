package com.moromoro.heliopause.event;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.render.CrucibleBlockRenderer;
import com.moromoro.heliopause.render.OrbBlockRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//クライアント側の挙動を登録するところ
@Mod.EventBusSubscriber(modid = Heliopause.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(Heliopause.CRUCIBLE_BE.get(), CrucibleBlockRenderer::new);
        event.registerBlockEntityRenderer(Heliopause.ORB_BE.get(), OrbBlockRenderer::new);
    }
}
