package com.moromoro.heliopause.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.network.KeyPacket;
import com.moromoro.heliopause.network.NetworkChannel;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.BitSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum KeyMapRegistry {

    BOTTLE_DRAIN("bottle_drain", GLFW.GLFW_KEY_LEFT_SHIFT),
    CIRCLE_SELECT("circle_select",GLFW.GLFW_KEY_LEFT_ALT),
    ;

    private KeyMapping keyMapping;
    private final String keyApplication;
    private final int keyCode;
    
    // サーバー用パケット系
    private static final ConcurrentMap<UUID, BitSet> ServerKeyRequests = new ConcurrentHashMap<>();
    private static BiConsumer<Integer, InteractionHand> ClientSendPacket = (idx, hand) -> {};

    KeyMapRegistry(String application, int defaultKey){
        this.keyApplication = "keybinding."+ Heliopause.MODID + "."+ application;
        this.keyCode = defaultKey;
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event){
        for( KeyMapRegistry key : values()){
            key.keyMapping = new KeyMapping(key.keyApplication, key.keyCode, Heliopause.MODID);

            event.register(key.keyMapping);
        }
        
        // パケット送信
        ClientSendPacket = (keyIndex, hand) -> {
            NetworkChannel.CHANNEL.sendToServer(new KeyPacket(keyIndex, hand));
        };
    }

    public KeyMapping getKeyMapping(){
        return keyMapping;
    }

    public boolean isPressed(){
        return keyMapping.isDown();
    }
    public static boolean isPressed(int key){
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(),key);
    }
    
    // パケット受信
    public static void receiveServerRequest(ServerPlayer player, int keyIndex) {
        if (player == null) return;
        UUID id = player.getUUID();
        ServerKeyRequests.compute(id, (uuid, bitSet) -> {
            if (bitSet == null) bitSet = new BitSet(values().length);
            if (keyIndex >= 0 && keyIndex < values().length) bitSet.set(keyIndex);
            return bitSet;
        });
    }
    
    public boolean isActivated(Level level, Player player, InteractionHand hand) {
        if (level == null) {
            return false;
        }
        // クライアント側
        if (level.isClientSide) {
            if(isPressed(this.keyCode)){
                // パケット送信
                ClientSendPacket.accept(this.ordinal(), hand);
                return true;
            }
            return false;
        }
        
        // サーバー側
        if (player == null) {
            return false;
        }
        UUID id = player.getUUID();
        BitSet bitSet = ServerKeyRequests.get(id);
        if (bitSet == null) {
            return false;
        }
        
        int index = this.ordinal();
        synchronized (bitSet) {
            if (index < bitSet.length() && bitSet.get(index)) {
                // 消費
                bitSet.clear(index);
                if (bitSet.isEmpty()) {
                    ServerKeyRequests.remove(id, bitSet);
                }
                return true;
            }
        }
        return false;
    }
    
    // フラグリセット
    public static void clearServerRequestsFor(ServerPlayer player) {
        if (player == null) return;
        ServerKeyRequests.remove(player.getUUID());
    }
}
