package com.moromoro.heliopause.item;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.RoastingTableBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.screen.MagicCircleAssemblyMenu;
import com.moromoro.heliopause.screen.MagicCircleAssemblyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChalkItem extends Item implements IhasBlockHoverTexts{

    private MagicCircleAssemblyMenu menu;

    public ChalkItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull List<Component> getBlockHoverTexts(ClientLevel clientLevel, ItemStack itemStack, BlockPos pos) {
        List<Component> tooltip = new ArrayList<>();
        Minecraft instance = Minecraft.getInstance();
        BlockState blockState = clientLevel.getBlockState(pos);
        if(blockState.isAir()){return tooltip;}
        //操作キーを取得
        Component useKey = instance.options.keyUse.getTranslatedKeyMessage();
        //tooltip.add(Component.literal("[").append(useKey).append("] :"));
        tooltip.add(Component.literal("[").append(Component.translatable("item.heliopause.chalk.tooltip.description1",useKey)).append("] :"));
        tooltip.add(Component.translatable("item.heliopause.chalk.tooltip.description2"));
        return tooltip;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int p_41431_) {
        super.onUseTick(level, entity, itemStack, p_41431_);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        //レベルを取得
        Level level = context.getLevel();
        Direction direction = context.getClickedFace();
        BlockPos pos = context.getClickedPos().below();
        //縦のブロックを取得(テスト用)
        BlockState[] OverBlocks = new BlockState[8];
        for (int i = 0; i < 8; i++) {
            OverBlocks[i]= level.getBlockState(pos.above(i));
        }
        //レシピのチェック
        Boolean Check = true;
        for (int i = 0; i < 8; i++) {
            switch (i){
                case 0:
                    if(!OverBlocks[i].is(BlockTags.LOGS)){Check = false;}
                    break;
                case 1:
                    if(!OverBlocks[i].is(Blocks.COPPER_BLOCK)){Check = false;}
                    break;
                case 7:
                    if(!OverBlocks[i].is(Blocks.AIR)){Check = false;}
                    break;
                default:
                    if(!OverBlocks[i].is(BlockTags.PLANKS)){Check = false;}
                    break;
            }
            Heliopause.LOGGER.debug(OverBlocks[i].toString());
            Heliopause.LOGGER.debug(Check.toString());
        }
        if(!Check){return InteractionResult.PASS;}
        //置き換える
        level.setBlock(
                pos.above(7),
                BlockRegistry.FLUID_SPREADER_ORB.get().defaultBlockState(),
                0
        );
        for (int i = 0; i < 8; i++) {
            level.destroyBlock(pos.above(i),false);
            if (i == 7) {
                level.setBlock(
                        pos.above(i),
                        BlockRegistry.FLUID_SPREADER_ORB.get().defaultBlockState(),
                        0
                );
            } else {
                level.setBlock(
                        pos.above(i),
                        BlockRegistry.FLUID_SPREADER_TOWER.get().defaultBlockState()
                                .setValue(BlockStateProperties.LEVEL, i)
                                .trySetValue(BlockStateProperties.HORIZONTAL_FACING, direction),
                        0
                );
            }
        }

        return InteractionResult.SUCCESS;
        //NetworkHooks.openScreen((ServerPlayer) context.getPlayer(), );
    }
}
