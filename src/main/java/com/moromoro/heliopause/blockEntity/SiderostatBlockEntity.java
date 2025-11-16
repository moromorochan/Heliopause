package com.moromoro.heliopause.blockEntity;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.SiderostatBlock;
import com.moromoro.heliopause.recipe.MoonlightPouringRecipe;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import com.moromoro.heliopause.screen.SiderostatMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SiderostatBlockEntity extends BlockEntity implements MenuProvider {

    // 表示バウンディングボックス
    private static final AABB RENDER_SHAPE = new AABB(-0.5D, -0.5D, 0,1.5D,1.5D,1);
    // 表示メニュー
    private SiderostatMenu menu;

    // ゼンマイ量 0~180(角度)
    private int springAmount = 180;
    // ゼンマイ巻き状態
    private int springCharge = 0;
    // 角度が合っているか
    private boolean angleSynced = false;
    // 加工の進捗
    private int craftingProgress = 0;
    // 加工の要求
    private int craftingTotalTime = 0;

    // 視野の状況
    private short canSeeSkies = 0b00000000;
    private static final double[] CHECK_ANGLES = new double[]{
        11.25, 33.75, 56.25, 78.75, 101.25, 123.75, 146.25, 168.75
    };
    private static final double RAY_MAX_DISTANCE = 5.0 * 16.0;
    private static final int NEAR_STEP_COUNT = 24;   // 近距離の刻み
    private static final int CHUNK_SIZE = 16;
    private static final int FAR_CHUNK_SAMPLES = 5;  // 確認チャンク数

    // 内部アイテム(材料・完成品)
    private final ItemStackHandler itemHandler = new ItemStackHandler(2){
        // 内容更新毎にセーブ
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            SiderostatBlockEntity.this.setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        // アイテム搬入できるかどうか制御
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            ItemStack existingStack = this.getStackInSlot(slot);

            // 既存のスタックが空、または同種のアイテムでスタックが満杯でない場合
            if (
                existingStack.isEmpty() ||
                    (
                        (existingStack.getItem() == stack.getItem()) &&
                            (existingStack.getCount() < existingStack.getMaxStackSize())
                    )
            ) {
                return super.insertItem(slot, stack, simulate);
            } else {
                // 種類が一致しないか、スタックが満杯の場合
                return stack;
            }
        }
    };

    // 呼び出し時の構築用
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    public static final int DATA_ACCESS_LENGTH = 5;
    public static final int SPRING_AMOUNT_INDEX = 0;
    public static final int ANGLE_SYNCED = 1;
    public static final int CRAFTING_PROGRESS_INDEX = 2;
    public static final int CRAFTING_TOTAL_TIME_INDEX = 3;
    public static final int CAN_SEE_SKIES_INDEX = 4;
    //データ格納用
    //private int springAmount = 0;
    //private int craftingDuration = 0;
    //private int craftingTime = 0;
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index){
                case SPRING_AMOUNT_INDEX -> SiderostatBlockEntity.this.springAmount;
                case ANGLE_SYNCED -> SiderostatBlockEntity.this.angleSynced? 1:0;
                case CRAFTING_PROGRESS_INDEX -> SiderostatBlockEntity.this.craftingProgress;
                case CRAFTING_TOTAL_TIME_INDEX -> SiderostatBlockEntity.this.craftingTotalTime;
                case CAN_SEE_SKIES_INDEX -> SiderostatBlockEntity.this.canSeeSkies;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index){
                case SPRING_AMOUNT_INDEX -> SiderostatBlockEntity.this.springAmount = value;
                case ANGLE_SYNCED -> SiderostatBlockEntity.this.angleSynced = (value == 1);
                case CRAFTING_PROGRESS_INDEX -> SiderostatBlockEntity.this.craftingProgress = value;
                case CRAFTING_TOTAL_TIME_INDEX -> SiderostatBlockEntity.this.craftingTotalTime = value;
                case CAN_SEE_SKIES_INDEX -> SiderostatBlockEntity.this.canSeeSkies = (short) value;
            };
        }

        @Override
        public int getCount() {
            return DATA_ACCESS_LENGTH;
        }
    };// 0:現在指している時刻 1:角度が合っているか 2:クラフトの要求長さ 3:クラフトの残り 4:視界状況

    public SiderostatBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.SIDEROSTAT_BE.get(), blockPos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return RENDER_SHAPE.move(this.getBlockPos());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put("Slot", itemHandler.serializeNBT());
        nbt.putInt("SpringAmount",this.springAmount);
        nbt.putInt("SpringCharge",this.springCharge);
        nbt.putBoolean("AngleSynced",this.angleSynced);
        nbt.putInt("CraftingProgress",this.craftingProgress);
        nbt.putInt("CraftingTotalTime",this.craftingTotalTime);
        nbt.putShort("CanSeeSkies",this.canSeeSkies);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("Slot"));
        springAmount = nbt.getInt("SpringAmount");
        springCharge = nbt.getInt("SpringCharge");
        angleSynced = nbt.getBoolean("AngleSynced");
        craftingProgress = nbt.getInt("CraftingProgress");
        craftingTotalTime = nbt.getInt("CraftingTotalTime");
        this.canSeeSkies = nbt.getShort("CanSeeSkies");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

   /* private Function<BlockEntity, CompoundTag> SavePacket() {
        return blockEntity ->{
            CompoundTag nbt = new CompoundTag();
            if(blockEntity instanceof SiderostatBlockEntity entity){
                nbt.putInt("SpringAmount", entity.springAmount);
                nbt.putBoolean("AngleSynced", entity.angleSynced);
            }
            return nbt;
        };
    }*/

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        super.handleUpdateTag(nbt);
        this.load(nbt);
    }

    public void drops(){
        SimpleContainer inventory =new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.heliopause.siderostat");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        menu = new SiderostatMenu(containerId, playerInventory, this, this.data);
        return menu;
    }

    protected short batchCheckSightBits() {
        if (level == null) return 0;

        // 原点（ブロック中心）
        double ox = worldPosition.getX();
        double oy = worldPosition.getY();
        double oz = worldPosition.getZ();

        short mask = 0;

        for (int i = 0; i < CHECK_ANGLES.length; i++) {
            double deg = CHECK_ANGLES[i];
            double rad = Math.toRadians(deg);

            // 東西-天頂方向のみ（Z成分は 0）
            double dx = Math.cos(rad);
            double dy = Math.sin(rad);

            Vec3 start = new Vec3(ox + dy* 0.7, oy + dy * 0.7, oz);
            Vec3 end = new Vec3(ox + dx * RAY_MAX_DISTANCE, oy + dy * RAY_MAX_DISTANCE, oz);

            // ClipContext のブロックモードは COLLIDER（衝突ボックス）を利用
            ClipContext context = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null);
            BlockHitResult result = level.clip(context);

            //Heliopause.LOGGER.debug(level.getBlockState());
            // 遮蔽がないなら
            if (result.getType() == HitResult.Type.MISS) {
                mask |= (short) (1 << i);
            }
        }

        // フィールドへ保存して同期
        this.canSeeSkies = mask;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return mask;

    }

    // ゼンマイ巻く
    public void chargeSpring(){
        if(springAmount > 0 && !angleSynced){
            springCharge += 23;
            if(level != null){
                level.playSound(Minecraft.getInstance().player,worldPosition, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.BLOCKS, 1.0f,0.2f);
            }
            /*angleSynced = false;
            springCharge -= 5;
            level.playSound(Minecraft.getInstance().player,worldPosition, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.BLOCKS, 1.0f,0.2f);
            setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }*/
        }
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, SiderostatBlockEntity blockEntity){
        if(level.isClientSide()){
            return;
        }
        // 月の角度を取り出す
        double currentMoonAngle = (level.getTimeOfDay(1.0F) * 360 + 270) % 360;
        boolean isNight = Math.ceil(currentMoonAngle) > 0 && currentMoonAngle < 180;
        // 昼はインジケータを無効化
        if(!isNight){
            canSeeSkies = 0;
        }
        // ブロックステートから稼働状態を取り出す
        Direction state = blockState.getValue(SiderostatBlock.FACING_SIDEROSTAT);

        // 夜の始まりに自動で起動
        if (springAmount <= 0 && state == Direction.EAST && isNight){
            BlockState newState = blockState.setValue(SiderostatBlock.FACING_SIDEROSTAT,Direction.UP);
            level.setBlock(pos, newState, 3);
            level.playSound(Minecraft.getInstance().player, pos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f,1.0f);
            angleSynced = true;
        }
        // ゼンマイが巻かれ始めたとき
        else if(springCharge > 0 && state == Direction.WEST){
            BlockState newState = blockState.setValue(SiderostatBlock.FACING_SIDEROSTAT,Direction.UP);
            level.setBlock(pos, newState, 3);
            level.playSound(Minecraft.getInstance().player, pos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f,1.0f);
        }
        // 稼働状態で
        else if(state == Direction.UP){

            // 角度が揃っていないとき
            if(!angleSynced){

                // 夜かつ遅れているなら進める
                if(isNight && springAmount < currentMoonAngle){
                    ++springAmount;
                    // 追いついたらSync
                    if(springAmount > currentMoonAngle){
                        angleSynced = true;
                    }
                    //level.playSound(Minecraft.getInstance().player, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.5f,0.5f);
                }
                else if(springCharge > 0){
                    // チャージ
                    springAmount -= (int)Math.ceil(springCharge/2.0);
                    springCharge -= (int)Math.ceil(springCharge/2.0);
                    // 戻しすぎない
                    if(isNight && springAmount < currentMoonAngle){
                        angleSynced = true;
                        springCharge = 0;
                    }
                    // ゼンマイが巻かれきったなら停止
                    if(springAmount <= 0){
                        springAmount = 0;
                        springCharge = 0;
                        BlockState newState = blockState.setValue(SiderostatBlock.FACING_SIDEROSTAT,Direction.EAST);
                        level.setBlock(pos, newState, 3);
                        level.playSound(Minecraft.getInstance().player, pos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f,1.0f);
                    }
                }
            }
            // 揃っているとき
            else{
                // 視野チェック
                canSeeSkies = batchCheckSightBits();

                // 合わせる
                springAmount = (int)Math.floor(currentMoonAngle);

                // レシピ挙動
                if(
                    (isAngleVisible(currentMoonAngle, canSeeSkies))
                    && !level.isRaining() && !level.isThundering()
                ){
                    SimpleContainer inventory =new SimpleContainer(itemHandler.getSlots());
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        inventory.setItem(i, itemHandler.getStackInSlot(i));
                    }

                    // 材料が空でないなら
                    if (!inventory.getItem(1).isEmpty()) {
                        // レシピ確認
                        Optional<MoonlightPouringRecipe> optional =
                            level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.MOONLIGHT_POURING.get(), inventory, level);
                        if(optional.isPresent()){
                            // レシピを取得
                            MoonlightPouringRecipe recipe = optional.get();
                            craftingTotalTime = recipe.getCraftTime();
                            // 空と角度が適切なら
                            /*if()*/{
                                // 進捗を追加
                                ++craftingProgress;
                                // 進捗が完了したら
                                if(craftingProgress >= craftingTotalTime){
                                    // 結果アイテムを取得して追加
                                    ItemStack tempStack = recipe.assemble(inventory, level.registryAccess());
                                    itemHandler.insertItem(0, tempStack, false);
                                    // 材料アイテムを消費して進捗リセット
                                    if(!tempStack.isEmpty()){
                                        inventory.getItem(1).shrink(1);
                                        craftingProgress = 0;
                                    }
                                    // 入れられないなら進捗を待機
                                    else{
                                        craftingProgress = craftingTotalTime;
                                    }
                                }
                        /*setChanged();
                        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);*/
                            }
                        }
                    }
                }
            }

            // クライアントに送信
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

            // 夜明け、ゼンマイが最大角度になったら停止
            if(springAmount > 180 && !isNight){
                BlockState newState = blockState.setValue(SiderostatBlock.FACING_SIDEROSTAT,Direction.WEST);
                level.setBlock(pos, newState, 3);
                level.playSound(Minecraft.getInstance().player, pos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f,1.0f);

                // 停止状態に
                springAmount = 180;
                springCharge = 0;
                angleSynced = false;
            }
        }
    }

    private boolean isAngleVisible(double angleDeg, short sight){
        // 11.25度刻みのスライス
        int index = (int)(angleDeg/22.5);
        // 180度は最後のスライスに含める
        if(index >=8)index = 7;
        // ビット判定
        return (sight & (1 << index)) != 0;
    }

    public int getSpringAmount() {
        return springAmount;
    }

    public int getCraftingProgress() {
        return craftingProgress;
    }

    public int getCraftingTotalTime() {
        return craftingTotalTime;
    }

    public boolean getSynced() {
        return angleSynced;
    }

    public int getSpringCharge() {
        return springCharge;
    }

    public short getCanSeeSkies() {
        return canSeeSkies;
    }
}
