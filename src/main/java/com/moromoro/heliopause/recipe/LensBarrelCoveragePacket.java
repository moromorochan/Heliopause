package com.moromoro.heliopause.recipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LensBarrelCoveragePacket {
    private final Map<ResourceLocation, LensBarrelCoverageListener.BarrelCoverageData> data;

    public LensBarrelCoveragePacket(Map<ResourceLocation, LensBarrelCoverageListener.BarrelCoverageData> data) {
        this.data = data;
    }

    public static void encode(LensBarrelCoveragePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.data.size());
        msg.data.forEach((id, data) -> {
            buf.writeResourceLocation(id);
            buf.writeCollection(data.block(), FriendlyByteBuf::writeUtf);
            buf.writeUtf(data.icon());
            buf.writeVarIntArray(data.icon_color());
            buf.writeUtf(data.name());
        });
    }

    public static LensBarrelCoveragePacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, LensBarrelCoverageListener.BarrelCoverageData> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            List<String> tag = buf.readList(FriendlyByteBuf::readUtf);
            String icon = buf.readUtf();
            int[] color = buf.readVarIntArray();
            String features = buf.readUtf();

            map.put(id, new LensBarrelCoverageListener.BarrelCoverageData(tag, icon, color, features));
        }

        return new LensBarrelCoveragePacket(map);
    }

    public static void handle(LensBarrelCoveragePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LensBarrelCoverageListener.replace(msg.data);
        });
        ctx.get().setPacketHandled(true);
    }
}
