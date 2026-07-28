package com.moromoro.heliopause.event;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.network.NetworkChannel;
import com.moromoro.heliopause.network.LensBarrelCoverageListener;
import com.moromoro.heliopause.network.LensBarrelCoveragePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Heliopause.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventBusServerEvents {
    
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        
        // サーバ側データをコピーして送信
        var mapCopy = new java.util.HashMap<>(LensBarrelCoverageListener.getMap());
        
        NetworkChannel.CHANNEL.send(
            PacketDistributor.PLAYER.with(() -> serverPlayer),
            new LensBarrelCoveragePacket(mapCopy)
        );
        
        Heliopause.LOGGER.info("Sent {} barrel coverage entries to {}", mapCopy.size(), serverPlayer.getName().getString());
    }
    
}
