package com.moromoro.heliopause.item;

import com.moromoro.ConfigHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class FluidBottle extends Item {

    private static final String FLUID_NBT_KEY = "FluidStack";
    public static final String COLOR_NBT_KEY = "color";
    public static final String MODEL_NBT_KEY = "CustomModelData";
    protected FluidTank mainTank;
    protected int USE_AMOUNT;

    public FluidBottle(Properties properties, int capacity,int useAmount) {
        super(properties);
        this.mainTank = new FluidTank(capacity);
        USE_AMOUNT = useAmount;
    }

    //液体の色をconfigから取得
    private int getFluidColor(FluidStack stack){
        //液体のidを取得
        String fluidName = ForgeRegistries.FLUIDS.getKey(stack.getFluid()).toString();
        if(ConfigHolder.FLUID_COLORS.containsKey(fluidName)){
            return ConfigHolder.FLUID_COLORS.get(fluidName).get();
        }
        //idが無ければ、デフォルトカラーを返す
        return 0xFF00FF;
    }

    //アイテムの色を取得
    private int getNbtColor(ItemStack stack)
    {
        //nbtがないなら、紫色を返す
        if (!stack.hasTag()){
            return 0xFF00FF;
        }
        CompoundTag nbt = stack.getTag();
        //nbtに色情報がないなら、紫色(0xFF00FF)を返す(デバッグ用の緑色→0x50BC5F)
        if(!nbt.contains(COLOR_NBT_KEY)){
            return 0xFF00FF;
        }
        return nbt.getInt(FLUID_NBT_KEY);
    }

    //アイテムに色を適用
    private void setNbtColor(ItemStack stack, int color)
    {
        //NBTを取得(なければ生成)
        CompoundTag nbt = stack.getOrCreateTag();
        //色を保存
        nbt.putInt(COLOR_NBT_KEY,color);
    }

    //カスタムモデルデータ値を液体から用意する
    private int getFluidCustomModelVal(float fluidPercentage) {
        if (fluidPercentage == 0.0f) {
            return 0;
        } else if (fluidPercentage < 0.20f) {
            return 1;
        } else if (fluidPercentage < 0.40f) {
            return 2;
        } else if (fluidPercentage < 0.60f) {
            return 3;
        } else if (fluidPercentage < 0.80f) {
            return 4;
        } else if (fluidPercentage < 1.00f) {
            return 5;
        } else {
            return 6;
        }
    }
    //光る液体の見た目を使う場合のオプション
    private int getFluidCustomModelVal(float fluidPercentage,boolean bright) {
        if(bright)
        {
            if (fluidPercentage == 0.0f) {
                return 10;
            } else if (fluidPercentage < 0.20f) {
                return 11;
            } else if (fluidPercentage < 0.40f) {
                return 12;
            } else if (fluidPercentage < 0.60f) {
                return 13;
            } else if (fluidPercentage < 0.80f) {
                return 14;
            } else if (fluidPercentage < 1.00f) {
                return 15;
            } else {
                return 16;
            }
        }
        else
        {
            if (fluidPercentage == 0.0f) {
                return 0;
            } else if (fluidPercentage < 0.20f) {
                return 1;
            } else if (fluidPercentage < 0.40f) {
                return 2;
            } else if (fluidPercentage < 0.60f) {
                return 3;
            } else if (fluidPercentage < 0.80f) {
                return 4;
            } else if (fluidPercentage < 1.00f) {
                return 5;
            } else {
                return 6;
            }
        }
    }

    //カスタムモデルデータ値を用意
    private void setCustomModelDataValue(ItemStack stack,FluidStack fluidStack,int tankCapacity){
        //カスタムモデルデータ値を変更
        stack.getOrCreateTag().putInt(
                MODEL_NBT_KEY,
                getFluidCustomModelVal(
                        (float) fluidStack.getAmount() / tankCapacity,
                        (fluidStack.getFluid().getFluidType().getLightLevel() >= 7)
                )
        );
    }

    //nbtから液体情報を取得
    private FluidStack getNbtFluid(ItemStack stack){
        //nbtがないなら、空状態を返す
        if (!stack.hasTag()){
            return FluidStack.EMPTY;
        }
        CompoundTag nbt = stack.getTag();
        //nbtにタンク情報がないなら、空状態を返す
        if(!nbt.contains(FLUID_NBT_KEY)){
            return FluidStack.EMPTY;
        }
        return FluidStack.loadFluidStackFromNBT(nbt.getCompound(FLUID_NBT_KEY));
    }

    //nbtに液体情報を書き込み
    private void setNbtFluid(ItemStack stack, FluidStack fluid) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.put(FLUID_NBT_KEY,fluid.writeToNBT(new CompoundTag()));
    }

    //液体の出し入れ
    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        //ブロックエンティティのデータを取得
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = context.getLevel().getBlockEntity(pos);

        //ブロックエンティティ側が対応しているか確認
        if (blockEntity instanceof IFluidHandler blockFluidHandler) {
            //その他のデータを取得
            Player player = context.getPlayer();
            Level level = context.getLevel();
            ItemStack heldItem = player.getItemInHand(context.getHand());

            //効果音を鳴らしたかどうかを格納 一度だけ鳴らす
            boolean soundPlayed = false;
            //プレイヤー以外の操作はパス
            if(player==null){return InteractionResult.PASS;}
            //nbtを取り出す
            this.mainTank.setFluid(getNbtFluid(heldItem));
            //アイテムがひとつのときは、USE_AMOUNTずつ出し入れ
            if (heldItem.getCount()==1)
            {
                if (player.isShiftKeyDown()) {
                    // シフト右クリック: アイテムからブロックへ移す
                    // ブロックにどれだけ流し入れられるか確認 0なら動作を終わる
                    int fillAllowance = blockFluidHandler.getTankCapacity(0) - blockFluidHandler.getFluidInTank(0).getAmount();
                    if (fillAllowance > 0) {
                        // アイテムの液体をどれだけ取り出せるか確認
                        FluidStack drainAllowance = this.mainTank.drain(Math.min(USE_AMOUNT, fillAllowance), IFluidHandler.FluidAction.SIMULATE);
                        if (!drainAllowance.isEmpty()) {
                            // ブロックの液体を増やす
                            int filledAmount = blockFluidHandler.fill(drainAllowance, IFluidHandler.FluidAction.EXECUTE);
                            // 実際にブロックに追加された液体の量だけ、アイテムから液体を取り出す
                            this.mainTank.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);

                            // 効果音を再生
                            level.playSound(null, pos, drainAllowance.getFluid().getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                            //ブロックエンティティの更新を保存
                            blockEntity.setChanged();
                            //容量が0になった場合、nbtを削除
                            if(this.mainTank.getFluidInTank(0).isEmpty()){
                                ItemStack resultItem = new ItemStack(heldItem.getItem());
                                resultItem.setTag(null);
                                player.setItemInHand(context.getHand(),resultItem);
                            }else{
                                //アイテムのnbtを更新
                                setNbtFluid(heldItem, this.mainTank.getFluidInTank(0));
                                setCustomModelDataValue(heldItem,this.mainTank.getFluid(),this.mainTank.getCapacity());
                            }
                            return InteractionResult.SUCCESS;
                        }
                    }
                } else {
                    // 右クリック: ブロックからアイテムへ移す
                    // アイテムにどれだけ流し入れられるか確認 0なら動作を終わる
                    int fillAllowance = this.mainTank.getCapacity() - this.mainTank.getFluidAmount();
                    if (fillAllowance > 0) {
                        // ブロックの液体をどれだけ取り出せるか確認
                        FluidStack drainAllowance = blockFluidHandler.drain(Math.min(USE_AMOUNT, fillAllowance), IFluidHandler.FluidAction.SIMULATE);
                        if (!drainAllowance.isEmpty()) {
                            // アイテムの液体を増やす
                            int filledAmount = this.mainTank.fill(drainAllowance, IFluidHandler.FluidAction.EXECUTE);
                            // 実際にアイテムに追加された液体の量だけ、ブロックから液体を取り出す
                            blockFluidHandler.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);

                            // 効果音を再生
                            level.playSound(null, pos, drainAllowance.getFluid().getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                            //ブロックエンティティの更新を保存
                            blockEntity.setChanged();
                            //アイテムのnbtを更新
                            setNbtFluid(heldItem, this.mainTank.getFluidInTank(0));
                            setNbtColor(heldItem, getFluidColor(this.mainTank.getFluidInTank(0)));
                            setCustomModelDataValue(heldItem,this.mainTank.getFluid(),this.mainTank.getCapacity());

                            return InteractionResult.SUCCESS;
                        }
                    }
                }
                return InteractionResult.PASS;
            }
            else//アイテムがスタックされているときは、一度に移せるだけ移す
            {
                //アイテムの数を取得
                final int itemCount = heldItem.getCount();
                /*
                //アイテムの種類を取得
                final ItemLike itemLike = heldItem.getItemHolder().get();
                //扱う液体の種類を取得
                final Fluid handlingFluid = getNbtFluid(heldItem).getFluid();
                //ブロックからどれだけ取り出せるか取得
                int blockDrainAllowance = blockFluidHandler.getFluidInTank(0).getAmount();
                // ブロックにどれだけ流し入れられるか取得
                int blockFillAllowance = blockFluidHandler.getTankCapacity(0) - blockDrainAllowance;
                if(player.isShiftKeyDown()){
                    //シフト右:アイテムからブロックへ
                    //流し入れられる量が0なら動作を終わる
                    if (blockFillAllowance > 0) {
                        //アイテムの液体をどれだけ取り出せるか確認 空なら動作を終わる
                        int drainAllowance = this.mainTank.drain(this.mainTank.getCapacity(), IFluidHandler.FluidAction.SIMULATE).getAmount();
                        if(drainAllowance > 0){
                            //空になる瓶の数と端数の瓶の内容量、ブロックに残る量を計算
                            int emptyingAmount = (int) Math.min(itemCount,Math.floor(blockFillAllowance/drainAllowance));
                            int itemRemainingVolume = blockFillAllowance % drainAllowance;
                            int blockRemainingVolume = blockDrainAllowance + emptyingAmount * drainAllowance + itemRemainingVolume;
                            //空になったアイテムを与える
                            ItemStack resultEmptiedBottle = new ItemStack(itemLike,emptyingAmount);
                            player.addItem(resultEmptiedBottle);
                            //元のアイテムスタックの数を変更
                            heldItem.shrink(emptyingAmount);
                            //端数が残ったアイテムを与える 端数が無い場合スキップ
                            if(itemRemainingVolume >0)
                            {
                                ItemStack resultRemainingBottle = new ItemStack(itemLike);
                                //液体を用意
                                FluidStack resultRemainingFluid = new FluidStack(handlingFluid,itemRemainingVolume);
                                //アイテムのnbtを更新
                                setNbtFluid(resultRemainingBottle, resultRemainingFluid);
                                setNbtColor(resultRemainingBottle, getFluidColor(resultRemainingFluid));
                                setCustomModelDataValue(resultRemainingBottle,resultRemainingFluid.getAmount(),this.mainTank.getCapacity());
                                //元のアイテムスタックの数を変更
                                heldItem.shrink(1);
                            }
                            // 効果音を再生
                            level.playSound(null, pos, handlingFluid.getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);

                            //ブロックエンティティの更新
                            blockFluidHandler.getFluidInTank(0).setAmount(blockRemainingVolume);
                            //ブロックエンティティの更新を保存
                            blockEntity.setChanged();
                            return InteractionResult.SUCCESS;
                        }
                    }
                    return InteractionResult.PASS;
                }else{

                    //右: ブロックからアイテムへ
                    //取り出せる量が0なら動作を終わる
                    if (blockDrainAllowance > 0) {
                        //アイテムに液体をどれだけ入れられるか確認 空なら動作を終わる
                        int fillAllowance = this.mainTank.fill(this.mainTank.getFluid(), IFluidHandler.FluidAction.SIMULATE);
                        if (fillAllowance>0) {
                            //満タンになる瓶の数と端数の瓶の内容量、ブロックに残る量を計算
                            int fulfillingAmount = (int) Math.min(itemCount, Math.floor(blockDrainAllowance / fillAllowance));
                            int itemRemainingVolume = blockDrainAllowance % fillAllowance;
                            int blockRemainingVolume = blockDrainAllowance - ( fulfillingAmount * fillAllowance + itemRemainingVolume);
                            //満タンになったアイテムを与える
                            ItemStack resultFulfilledBottle = new ItemStack(itemLike, fulfillingAmount);
                            //液体を用意
                            FluidStack resultFulfilledFluid = new FluidStack(handlingFluid, this.mainTank.getCapacity());
                            //アイテムのnbtを更新
                            setNbtFluid(resultFulfilledBottle, resultFulfilledFluid);
                            setNbtColor(resultFulfilledBottle, getFluidColor(resultFulfilledFluid));
                            setCustomModelDataValue(resultFulfilledBottle, resultFulfilledFluid.getAmount(), this.mainTank.getCapacity());
                            player.addItem(resultFulfilledBottle);
                            //元のアイテムスタックの数を変更
                            heldItem.shrink(fulfillingAmount);
                            //端数が残ったアイテムを与える 端数が無い場合スキップ
                            if (itemRemainingVolume > 0) {
                                ItemStack resultRemainingBottle = new ItemStack(itemLike);
                                //液体を用意
                                FluidStack resultRemainingFluid = new FluidStack(handlingFluid,itemRemainingVolume);
                                //アイテムのnbtを更新
                                setNbtFluid(resultRemainingBottle, resultRemainingFluid);
                                setNbtColor(resultRemainingBottle, getFluidColor(resultRemainingFluid));
                                setCustomModelDataValue(resultRemainingBottle,resultRemainingFluid.getAmount(),this.mainTank.getCapacity());
                                //元のアイテムスタックの数を変更
                                heldItem.shrink(1);
                            }
                            // 効果音を再生
                            level.playSound(null, pos, handlingFluid.getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);

                            //ブロックエンティティの更新
                            blockFluidHandler.getFluidInTank(0).setAmount(blockRemainingVolume);
                            //ブロックエンティティの更新を保存
                            blockEntity.setChanged();
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
                */

                if(player.isShiftKeyDown()){
                    //シフト右:アイテムからブロックへ
                    for (int i = 0; i < itemCount; i++) {
                        // ブロックにどれだけ流し入れられるか確認 0なら動作を終わる
                        int fillAllowance = blockFluidHandler.getTankCapacity(0) - blockFluidHandler.getFluidInTank(0).getAmount();
                        if (fillAllowance <= 0) {break;}
                        // アイテムの液体をどれだけ取り出せるか確認 0なら動作を終わる
                        FluidStack drainAllowance = this.mainTank.drain(fillAllowance, IFluidHandler.FluidAction.SIMULATE);
                        if(drainAllowance.isEmpty()){break;}
                        //ブロックの液体を増やす
                        int filledAmount = blockFluidHandler.fill(drainAllowance, IFluidHandler.FluidAction.EXECUTE);

                        //アイテムを渡す
                        //アイテムを用意する
                        ItemStack resultBottle = new ItemStack(heldItem.getItem());
                        //液体の結果を用意する
                        FluidStack resultFluid = drainAllowance;
                        //容量が0になった場合、nbtは書き込まない
                        int resultAmount = this.mainTank.getFluidAmount()-filledAmount;
                        if(resultAmount!=0)
                        {
                            //もとの液体の量-ブロックに渡した液体の量
                            resultFluid.setAmount(resultAmount);
                            //アイテムのnbtを更新
                            setNbtFluid(resultBottle, resultFluid);
                            setNbtColor(resultBottle, getFluidColor(resultFluid));
                            setCustomModelDataValue(resultBottle,resultFluid,this.mainTank.getCapacity());
                        }

                        //プレイヤーに渡す 渡せなかった場合ドロップ
                        boolean addSucceed = player.addItem(resultBottle);
                        if (!addSucceed) {
                            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), resultBottle);
                            level.addFreshEntity(itemEntity);
                        }
                        //アイテムを1減らす
                        heldItem.shrink(1);

                        // 効果音を再生
                        if(!soundPlayed){
                            level.playSound(null, pos, drainAllowance.getFluid().getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                            soundPlayed=true;
                        }
                    }
                }else{
                    //右: ブロックからアイテムへ
                    for (int i = 0; i < itemCount; i++) {
                        // アイテムにどれだけ流し入れられるか確認 0なら動作を終わる
                        int fillAllowance = this.mainTank.getTankCapacity(0) - this.mainTank.getFluidInTank(0).getAmount();
                        if (fillAllowance <= 0) {break;}
                        // ブロックの液体をどれだけ取り出せるか確認 0なら動作を終わる
                        FluidStack drainAllowance = blockFluidHandler.drain(fillAllowance, IFluidHandler.FluidAction.SIMULATE);
                        if(drainAllowance.isEmpty()){break;}
                        //アイテムの液体を増やす
                        int filledAmount = this.mainTank.fill(drainAllowance, IFluidHandler.FluidAction.SIMULATE);

                        // 実際にアイテムに追加された液体の量だけ、ブロックから液体を取り出す
                        blockFluidHandler.drain(filledAmount, IFluidHandler.FluidAction.EXECUTE);


                        //アイテムを渡す
                        //液体の結果を用意する
                        FluidStack resultFluid = drainAllowance;
                        //もとの液体の量+増やした液体の量
                        resultFluid.setAmount(this.mainTank.getFluidAmount()+filledAmount);

                        //アイテムを用意する
                        ItemStack resultBottle = new ItemStack(heldItem.getItem());
                        //アイテムのnbtを更新
                        setNbtFluid(resultBottle, resultFluid);
                        setNbtColor(resultBottle, getFluidColor(resultFluid));
                        setCustomModelDataValue(resultBottle,resultFluid,this.mainTank.getCapacity());

                        //プレイヤーに渡す 渡せなかった場合ドロップ
                        boolean addSucceed = player.addItem(resultBottle);
                        if (!addSucceed) {
                            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), resultBottle);
                            level.addFreshEntity(itemEntity);
                        }
                        //アイテムを1減らす
                        heldItem.shrink(1);

                        // 効果音を再生
                        if(!soundPlayed){
                            level.playSound(null, pos, drainAllowance.getFluid().getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                            soundPlayed=true;
                        }
                    }
                }
                //ブロックエンティティの更新を保存
                blockEntity.setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
