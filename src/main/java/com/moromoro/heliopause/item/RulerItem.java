package com.moromoro.heliopause.item;

import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// 魔法陣を開始する道具
public class RulerItem extends Item implements IhasHoverTexts {
    public RulerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull List<Component> getBlockHoverTexts(ClientLevel clientLevel, ItemStack itemStack, HitResult hitResult) {
        List<Component> tooltip = new ArrayList<>();
        Minecraft instance = Minecraft.getInstance();
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState blockState = clientLevel.getBlockState(pos);
            if(!blockState.is(BlockRegistry.BLACKBOARD.get())){return tooltip;}
            //操作キーを取得
            Component useKey = instance.options.keyUse.getTranslatedKeyMessage();
            //tooltip.add(Component.literal("[").append(useKey).append("] :"));
            tooltip.add(Component.literal("[").append(Component.translatable("item.heliopause.ruler.tooltip.description1",useKey)).append("] :"));
            tooltip.add(Component.translatable("item.heliopause.ruler.tooltip.description2"));
        }
        return tooltip;
    }

    //ブロックをクリックしたとき
    @Override
    public InteractionResult useOn(UseOnContext context) {
        //レベルを取得
        Level level = context.getLevel();
        Direction direction = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockState blockState = level.getBlockState(pos);

        if(!stack.is(this)){
            return InteractionResult.PASS;
        }
        if(blockState.is(BlockRegistry.BLACKBOARD.get())){
            level.setBlock(pos,BlockRegistry.WRITTEN_BOARD.get().defaultBlockState(), 0);
        } else if (blockState.is(BlockRegistry.WRITTEN_BOARD.get())) {

        }

        return InteractionResult.SUCCESS;
    }
}
