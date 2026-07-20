package com.moromoro.heliopause.event;

import com.moromoro.heliopause.implementable.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class TooltipEventHandler {
    // オーバーレイレンダリングに追加する
    @SubscribeEvent
    public void onRenderGuiOverlayPost(RenderGuiEvent event) {
        Minecraft instance = Minecraft.getInstance();
        Player player = instance.player;
        if (player == null) {
            return;
        }
        HitResult hitResult = instance.hitResult;
        if (hitResult == null) {
            return;
        }
        //メインハンドアイテムを取得
        ItemStack mainHandItem = player.getMainHandItem();
        //オフハンドアイテムを取得
        ItemStack offHandItem = player.getOffhandItem();
        //両方とも空の場合スキップ
            /*if(mainHandItem.isEmpty() && offHandItem.isEmpty()){
                return;
            }*/
        // アイテム描画成功判定
        boolean itemDrawn = false;
        
        //メインハンドをチェック
        if(!mainHandItem.isEmpty()){
            itemDrawn |= drawTextOverlay(event, mainHandItem, hitResult);
            itemDrawn |= drawGraphicOverlay(event, mainHandItem, hitResult);
        }
        //オフハンドをチェック
        else if (!offHandItem.isEmpty()) {
            itemDrawn |= drawTextOverlay(event, mainHandItem, hitResult);
            itemDrawn |= drawGraphicOverlay(event, mainHandItem, hitResult);
        }
        
        if(itemDrawn || hitResult.getType().equals(HitResult.Type.MISS)){
            return;
        }
        
        ClientLevel level = instance.level;
        if (level == null) {
            return;
        }
        
        // ブロック描画成功判定
        boolean blockDrawn = false;
        
        // ブロックをチェック
        if(hitResult.getType().equals(HitResult.Type.BLOCK)){
            Vec3 angle = player.getLookAngle().normalize().scale(0.5);
            BlockPos pos = BlockPos.containing(hitResult.getLocation().add(angle));
            Block block = level.getBlockState(pos).getBlock();
            if(block instanceof IHasHoverDrawBlock iBlock){
                AbstractBlockTooltipRenderer renderer = iBlock.getRendererHoverGraphic();
                if(renderer != null){
                    blockDrawn = renderer.renderHoverGraphic(event, pos);
                }
                //blockDrawn = iBlock.renderHoverGraphic(event, pos);
            }

        }
        if(blockDrawn){
            return;
        }
        
        // エンティティをチェック
        if(hitResult.getType().equals(HitResult.Type.ENTITY)){
            List<Entity> entities =
                level.getEntitiesOfClass(Entity.class, new AABB(hitResult.getLocation(),hitResult.getLocation()).inflate(0.3), e -> true);
            for (Entity entity : entities) {
                if(entity instanceof IHasHoverDrawEntity iEntity){
                    iEntity.renderHoverGraphicWithEntity(event, level);
                }
            }
        }
    }
    
    // ツールチップレンダリングに追加する
    @SubscribeEvent
    public void OnRenderTooltip(RenderTooltipEvent.GatherComponents event){
        Minecraft instance = Minecraft.getInstance();
        if(instance.player==null || instance.level == null){
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        if (stack.getItem() instanceof IHasTooltipDraw iItem) {
            iItem.renderTooltip(event, instance.level, stack);
        }
    }

    private boolean drawTextOverlay(RenderGuiEvent event, ItemStack itemStack, HitResult hitResult){
        // アイテムのツールチップをオーバーレイに表示
        if(itemStack.getItem() instanceof IHasHoverTexts iItem){
            Minecraft instance = Minecraft.getInstance();
            List<Component> tooltipLists = (iItem.getBlockHoverTexts(instance.level, itemStack, hitResult));

            for (int i = 0; i < tooltipLists.size(); i++) {
                event.getGuiGraphics().drawString(
                    instance.font, tooltipLists.get(i),
                    instance.getWindow().getGuiScaledWidth()/2 +10,
                    instance.getWindow().getGuiScaledHeight()/2 -4 + i*10,
                    0xFFFFFF,true);
            }
            return true;
        }
        return false;
    }

    // アイテムのホバーGUIをオーバーレイに表示
    private boolean drawGraphicOverlay(RenderGuiEvent event, ItemStack itemStack, HitResult hitResult){
        if(itemStack.getItem() instanceof IHasHoverDraw iItem){
            Minecraft instance = Minecraft.getInstance();
            // アイテムの関数にeventを渡してレンダラを回す
            return iItem.renderHoverGraphic(event, instance.level, itemStack, hitResult);
        }
        return false;
    }

    // ワールド座標系にオーバーレイを描画
    @SubscribeEvent
    public boolean onRenderLevelStage(RenderLevelStageEvent event){
        Minecraft instance = Minecraft.getInstance();
        if(instance.player==null || instance.level == null){
            return false;
        }
        //プレイヤーが手に持っているアイテムを確認
        ItemStack itemStack = instance.player.getMainHandItem();
        if(itemStack.getItem() instanceof IHasLevelDraw iItem){
            return iItem.renderLevelGraphic(event, instance.level, itemStack);
        }
        return false;
    }
}
