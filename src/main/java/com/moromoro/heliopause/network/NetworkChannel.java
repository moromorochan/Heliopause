package com.moromoro.heliopause.network;

import com.moromoro.Heliopause;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkChannel {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Heliopause.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(
            id++,
            LensBarrelCoveragePacket.class,
            LensBarrelCoveragePacket::encode,
            LensBarrelCoveragePacket::decode,
            LensBarrelCoveragePacket::handle
        );
        
        CHANNEL.registerMessage(
            id++,
            KeyPacket.class,
            KeyPacket::encode,
            KeyPacket::decode,
            KeyPacket::handle
        );
    }

}
