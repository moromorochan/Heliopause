package com.moromoro.heliopause.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KeyPacket {
    private final int keyIndex;
    private final InteractionHand hand;
    
    public KeyPacket(int keyIndex, InteractionHand hand) {
        this.keyIndex = keyIndex;
        this.hand = hand;
    }
    
    public static void encode(KeyPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.keyIndex);
        buf.writeEnum(pkt.hand);
    }
    
    public static KeyPacket decode(FriendlyByteBuf buf) {
        return new KeyPacket(buf.readInt(), buf.readEnum(InteractionHand.class));
    }
    
    public static void handle(KeyPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            // サーバー側で受信フラグをセット（KeyMapRegistry 側で消費される）
            com.moromoro.heliopause.registry.KeyMapRegistry.receiveServerRequest(player, pkt.keyIndex);
        });
        ctx.get().setPacketHandled(true);
    }
}
