package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.AbstractFluidTankEntity;
import com.moromoro.heliopause.blockEntity.CrucibleBlockEntity;
import com.moromoro.heliopause.blockEntity.OrbBlockEntity;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFluidTankBlock extends BaseEntityBlock {

    public AbstractFluidTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    private void playFluidSound(Level level, BlockPos pos, Fluid fluid){
        // 効果音を再生
        if(fluid.getPickupSound().isPresent())
        {
            level.playSound(null, pos, fluid.getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }else{ //液体に効果音設定が無い場合、代わりに水の音で代用
            level.playSound(null,pos, Fluids.WATER.getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @NotNull
    @Override
    public InteractionResult use(
            @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player,
            @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }
        // 現在地のブロックエンティティを取得
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractFluidTankEntity) {
            IFluidHandler fluidHandler = (IFluidHandler) blockEntity;

            // バケツアイテムを持っているなら
            if (heldItem.getItem() instanceof BucketItem bucketItem) {
                //バケツが空なら
                if (bucketItem == Items.BUCKET) {
                    //取り出し可能か確認
                    FluidStack drainTest = fluidHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
                    // 取り出しが成功した場合 プレイヤーに中身入りのバケツを渡す
                    if (drainTest.getAmount() == FluidType.BUCKET_VOLUME) {
                        //取り出しを実行
                        FluidStack drained = fluidHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
                        // クリエイティブかつ同一NBTのバケツがインベントリにある場合追加しない
                        ItemStack filledBucket = new ItemStack(drained.getFluid().getBucket());
                        if (player.isCreative()) {
                            // プレイヤーのインベントリ内を走査 同じものがあれば処理を終わる
                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                ItemStack stackInSlot = player.getInventory().getItem(i);
                                if (ItemStack.isSameItemSameTags(stackInSlot, filledBucket)) {
                                    //効果音を再生
                                    playFluidSound(level,pos,drained.getFluid());
                                    //更新
                                    //crucibleBlockEntity.setChanged();
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }

                        //バケツがひとつなら
                        if (heldItem.getCount() == 1) {
                            player.setItemInHand(hand, new ItemStack(filledBucket.getItem()));
                        } else {
                            //アイテムを追加 追加が成功したかのboolを取得
                            boolean addSucceed = player.addItem(filledBucket);
                            if (!addSucceed) {
                                // プレイヤーのインベントリが一杯なら、バケツをドロップする
                                ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), filledBucket);
                                level.addFreshEntity(itemEntity);
                            }
                            if (!player.isCreative()) {
                                // バケツの数を1つ減らす
                                heldItem.shrink(1);
                            }
                        }
                        //効果音を再生
                        playFluidSound(level,pos,drained.getFluid());

                        //更新 ->エンティティ側で実装
                        //crucibleBlockEntity.setChanged();
                        return InteractionResult.SUCCESS;
                    }
                } else // バケツが中身入りなら
                {
                    Fluid fluid = bucketItem.getFluid();
                    //流し入れ可能か確認
                    int fillTest = fluidHandler.fill(new FluidStack(fluid, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.SIMULATE);
                    // 流し入れに成功したら、プレイヤーに空のバケツを渡す
                    if (fillTest == FluidType.BUCKET_VOLUME) {
                        //流し入れを実行
                        int filled = fluidHandler.fill(new FluidStack(fluid, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                        // クリエイティブの場合は入れ替えない
                        if (!player.isCreative()) {
                            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        }
                        //効果音を再生
                        playFluidSound(level,pos,fluid);
                        //更新 ->エンティティ側で実装
                        //crucibleBlockEntity.setChanged();
                        return InteractionResult.SUCCESS;
                    }
                }
                //バケツでの出し入れに失敗した場合、誤設置を防ぐ
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }
}

