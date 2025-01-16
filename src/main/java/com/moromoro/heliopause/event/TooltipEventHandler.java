package com.moromoro.heliopause.event;

import com.moromoro.heliopause.item.IhasHoverTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class TooltipEventHandler {
    @SubscribeEvent
    public void onRenderGuiOverlayPost(RenderGuiOverlayEvent event) {
        Minecraft instance = Minecraft.getInstance();
        Player player = instance.player;
        if (player != null ) {
            HitResult hitResult = instance.hitResult;
            if (hitResult != null /*&& hitResult.getType() == HitResult.Type.BLOCK*/) {
                //BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                //メインハンドアイテムを取得
                ItemStack mainHandItem = player.getMainHandItem();
                //オフハンドアイテムを取得
                ItemStack offHandItem = player.getOffhandItem();
                //両方とも空の場合スキップ
                if(mainHandItem.isEmpty() && offHandItem.isEmpty()){
                    return;
                }
                List<Component> tooltipLists = new ArrayList<>();
                //メインハンドをチェック
                if(!mainHandItem.isEmpty() && mainHandItem.getItem() instanceof IhasHoverTexts){
                    tooltipLists = ((IhasHoverTexts)mainHandItem.getItem()).getBlockHoverTexts(instance.level, mainHandItem, hitResult);
                }
                //オフハンドをチェック
                else if (!offHandItem.isEmpty() && offHandItem.getItem() instanceof IhasHoverTexts) {
                    tooltipLists = ((IhasHoverTexts) offHandItem.getItem()).getBlockHoverTexts(instance.level, offHandItem, hitResult);
                }

                if(tooltipLists.isEmpty()){return;}

        //ブロックの名前をオーバーレイメッセージに表示
                for (int i = 0; i < tooltipLists.size(); i++) {
                    //boolean isShadowed = (i == 0);  // 一行目だけ影付きにする
                    event.getGuiGraphics().drawString(
                            instance.font, tooltipLists.get(i),
                            instance.getWindow().getGuiScaledWidth()/2 +10,
                            instance.getWindow().getGuiScaledHeight()/2 -4 + i*10,
                            0xFFFFFF,true);
                }
            }
        }
    }
}
