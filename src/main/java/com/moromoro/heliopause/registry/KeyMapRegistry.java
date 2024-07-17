package com.moromoro.heliopause.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.moromoro.Heliopause;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static com.mojang.blaze3d.platform.InputConstants.isKeyDown;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum KeyMapRegistry {

    BOTTLE_DRAIN("bottle_drain", GLFW.GLFW_KEY_LEFT_SHIFT),

    ;

    private KeyMapping keyMapping;
    private String keyApplication;
    private int keyCode;

    private KeyMapRegistry(String application, int defaultKey){
        this.keyApplication = "keybinding."+ Heliopause.MODID + "."+ application;
        this.keyCode = defaultKey;
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event){
        for( KeyMapRegistry key : values()){
            key.keyMapping = new KeyMapping(key.keyApplication, key.keyCode, Heliopause.MODNAME);

            event.register(key.keyMapping);
        }
    }

    public KeyMapping getKeyMapping(){
        return keyMapping;
    }

    public boolean isPressed(){
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(),keyCode);
    }
    public boolean isPressed(int key){
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(),key);
    }
}
