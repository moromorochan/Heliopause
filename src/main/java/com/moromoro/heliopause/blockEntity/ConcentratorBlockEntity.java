package com.moromoro.heliopause.blockEntity;

import com.moromoro.ConfigHolder;
import com.moromoro.heliopause.block.ConcentratorBlock;
import com.moromoro.heliopause.block.LensBarrelBlock;
import com.moromoro.heliopause.entity.LensBarrelEntity;
import com.moromoro.heliopause.generic.Season;
import com.moromoro.heliopause.generic.StackControl;
import com.moromoro.heliopause.network.LensBarrelCoverageListener.BarrelCoverageData;
import com.moromoro.heliopause.recipe.StarlightConcentrationRecipe;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import com.moromoro.heliopause.screen.ConcentratorMenu;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.minecraftforge.fluids.capability.IFluidHandler.*;

public class ConcentratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_BARREL_LENGTH = ConfigHolder.MAX_BARREL_LENGTH.get();
    
    // メニュー渡し用データ ワールド依存なのでnbtには保存しない
    private Set<BarrelCoverageData> barrelCoverage;
    private boolean isSyncedToStar;
    
    // 日時表示
    private long date;
    private long dayTime;
    
    // 進行中レシピ
    private StarlightConcentrationRecipe currentRecipe = null;
    // 進行度
    private int progress = 0;
    // レシピ時間
    private int maxProgress = 0;

    // アイテム・液体スロット
    public static final int SLOT_INPUT_ITEM = 0;
    public static final int SLOT_OUTPUT_ITEM = 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2){
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ConcentratorBlockEntity.this.setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        // アイテム搬入できるかどうか制御
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // 搬出専用のスロット判定
            /*if(slot == SLOT_OUTPUT_ITEM){
                return stack;
            }*/
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
    public static final int TANK_CAPACITY = 2000;
    private static final int TANK_COUNT = 2;
    public static final int SLOT_INPUT_FLUID = 0;
    public static final int SLOT_OUTPUT_FLUID = 1;
    private static class FluidTankHandler implements IFluidHandler{
        private final FluidTank[] fluidTanks = new FluidTank[]{
        new FluidTank((TANK_CAPACITY)),
        new FluidTank((TANK_CAPACITY))
        };

        public CompoundTag tanksWriteToNbt(CompoundTag nbt){
            for (int i = 0; i < fluidTanks.length; i++) {
                CompoundTag tankNbt = new CompoundTag();
                fluidTanks[i].writeToNBT(tankNbt);
                nbt.put(String.valueOf(i), tankNbt);
            }
            return nbt;
        }

        public void tanksReadFromNbt(CompoundTag nbt){
            for (int i = 0; i < fluidTanks.length; i++) {
                String key = String.valueOf(i);
                if(nbt.contains(key)){
                    fluidTanks[i].readFromNBT(nbt.getCompound(key));
                }
                else {
                    fluidTanks[i].setFluid(FluidStack.EMPTY);
                }
            }
        }

        @Override
        public int getTanks() {
            return fluidTanks.length;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if(tank < 0 || tank >= TANK_COUNT){
                return FluidStack.EMPTY;
            }
            return fluidTanks[tank].getFluidInTank(0);
        }

        @Override
        public int getTankCapacity(int tank) {
            if(tank < 0 || tank >= TANK_COUNT){
                return 0;
            }
            return fluidTanks[tank].getTankCapacity(0);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            if(tank < 0 || tank >= TANK_COUNT){
                return false;
            }
            return fluidTanks[tank].isFluidValid(stack);
        }

        public int fillTo(int tank, FluidStack resource, FluidAction action){
            if(tank < 0 || tank >= TANK_COUNT){return 0;}
            return fluidTanks[tank].fill(resource, action);
        }

        public FluidStack drainFrom(int tank, FluidStack resource, FluidAction action){
            if(tank < 0 || tank >= TANK_COUNT){return FluidStack.EMPTY;}
            return fluidTanks[tank].drain(resource, action);
        }

        public FluidStack drainFrom(int tank, int maxDrain, FluidAction action){
            if(tank < 0 || tank >= TANK_COUNT){return FluidStack.EMPTY;}
            return fluidTanks[tank].drain(maxDrain, action);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            // 入力タンクのみ
            return fillTo(SLOT_INPUT_FLUID, resource, action);
            /*for (FluidTank fluidTank : fluidTanks) {
                int amount = fluidTank.fill(resource, action);
                if(amount > 0){
                    return amount;
                }
            }
            return 0;*/
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            // 種類指定があるなら順番に
            //return drainFrom(SLOT_OUTPUT_FLUID, resource, action);
            for (FluidTank fluidTank : fluidTanks) {
                FluidStack drained = fluidTank.drain(resource, action);
                if(!drained.isEmpty()){
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            // 出力タンクのみ
            return drainFrom(SLOT_OUTPUT_FLUID, maxDrain, action);
            /*for (FluidTank fluidTank : fluidTanks) {
                FluidStack drained = fluidTank.drain(maxDrain, action);
                if(!drained.isEmpty()){
                    return drained;
                }
            }
            return FluidStack.EMPTY;*/
        }
    };
    private final FluidTankHandler fluidHandler = new FluidTankHandler();
    public FluidStack getInputFluid() {
        return fluidHandler.getFluidInTank(SLOT_INPUT_FLUID);
    }
    public FluidStack getOutputFluid() {
        return fluidHandler.getFluidInTank(SLOT_OUTPUT_FLUID);
    }
    
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    //private int recipeTimer = 0;
    //private static final int RECIPE_FREQ = 20; // 1秒毎

    // long値は上下に分割して送る
    public static final int DATA_ACCESS_LENGTH = 7;
    public static final int DATE_LOW = 0;
    public static final int DATE_HIGH = 1;
    public static final int TIME_LOW = 2;
    public static final int TIME_HIGH = 3;
    public static final int PROGRESS = 4;
    public static final int CAN_SEE_SKY = 5;
    public static final int MAX_PROGRESS = 6;

    // GUI用データ格納
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index){
                case DATE_LOW -> (int) (ConcentratorBlockEntity.this.date & 0xFFFFFFFFL);
                case DATE_HIGH -> (int) (ConcentratorBlockEntity.this.date >>> 32);
                case TIME_LOW -> (int) (ConcentratorBlockEntity.this.dayTime & 0xFFFFFFFFL);
                case TIME_HIGH -> (int) (ConcentratorBlockEntity.this.dayTime >>> 32);
                case PROGRESS -> ConcentratorBlockEntity.this.progress;
                case MAX_PROGRESS -> ConcentratorBlockEntity.this.maxProgress;
                case CAN_SEE_SKY -> ConcentratorBlockEntity.this.isSyncedToStar?1:0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATE_LOW -> {
                    long high = ConcentratorBlockEntity.this.date >>> 32;
                    ConcentratorBlockEntity.this.date = (high << 32) | (value & 0xFFFFFFFFL);
                }
                case DATE_HIGH -> {
                    long low = ConcentratorBlockEntity.this.date & 0xFFFFFFFFL;
                    ConcentratorBlockEntity.this.date = ((long) value << 32) | low;
                }
                case TIME_LOW -> {
                    long high = ConcentratorBlockEntity.this.dayTime >>> 32;
                    ConcentratorBlockEntity.this.dayTime = (high << 32) | (value & 0xFFFFFFFFL);
                }
                case TIME_HIGH -> {
                    long low = ConcentratorBlockEntity.this.dayTime & 0xFFFFFFFFL;
                    ConcentratorBlockEntity.this.dayTime = ((long) value << 32) | low;
                }
                case PROGRESS -> ConcentratorBlockEntity.this.progress = value;
                case MAX_PROGRESS -> ConcentratorBlockEntity.this.maxProgress = value;
                case CAN_SEE_SKY -> ConcentratorBlockEntity.this.isSyncedToStar = (value==1);
            }
        }

        @Override
        public int getCount() {
            return DATA_ACCESS_LENGTH;
        }
    };

    public ConcentratorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.CONCENTRATOR_BE.get(), blockPos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> fluidHandler);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if(side==Direction.UP){
            return super.getCapability(cap, side);
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if(side == null) {
                return lazyItemHandler.cast();
            }
            if(side == Direction.DOWN){
                return lazyItemHandler.lazyMap(map ->
                    StackControl.createFilteredItemHandler(map,IntArrayList.of(SLOT_OUTPUT_ITEM), false, true)).cast();
            }
            if(side.getAxis().isHorizontal()){
                return lazyItemHandler.lazyMap(map ->
                    StackControl.createFilteredItemHandler(map,IntArrayList.of(SLOT_INPUT_ITEM), true, true)).cast();
            }
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER){
            return lazyFluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        // 日時
        nbt.putLong("Date",date);
        nbt.putLong("DayTime",dayTime);
        // アイテム
        nbt.put("Items", itemHandler.serializeNBT());
        // 液体タンク
        CompoundTag tanksTag = new CompoundTag();
        fluidHandler.tanksWriteToNbt(tanksTag);
        nbt.put("FluidTanks", tanksTag);
        // レシピ進行
        nbt.putInt("Progress", progress);
        nbt.putInt("MaxProgress", maxProgress);
        // currentRecipe
        if (currentRecipe != null) {
            nbt.putString("CurrentRecipe", currentRecipe.getId().toString());
        }
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        // 日時
        date = nbt.getLong("Date");
        dayTime = nbt.getLong("DayTime");
        // アイテム
        if (nbt.contains("Items")) {
            itemHandler.deserializeNBT(nbt.getCompound("Items"));
        }
        // 液体タンク
        if (nbt.contains("FluidTanks")) {
            fluidHandler.tanksReadFromNbt(nbt.getCompound("FluidTanks"));
        }
        // レシピ進行
        progress = nbt.getInt("Progress");
        maxProgress = nbt.getInt("MaxProgress");
        // currentRecipe
        if (nbt.contains("CurrentRecipe")) {
            ResourceLocation id = new ResourceLocation(nbt.getString("CurrentRecipe"));
            if (level != null) {
                level.getRecipeManager().byKey(id).filter(recipe -> recipe instanceof StarlightConcentrationRecipe)
                    .ifPresent(recipe -> currentRecipe = (StarlightConcentrationRecipe) recipe);
            }
        }
        
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        super.handleUpdateTag(nbt);
        this.load(nbt);
    }

    public boolean assemble(){
        if(level == null){
            return false;
        }
        if(level.isClientSide()){
            return true;
        }
        // 下が魔法陣か確認
        /*if(!(level.getBlockEntity(worldPosition.below()) instanceof AltAzimuthCircleBoardBlockEntity)){
            return false;
        }*/
        // 連続する鏡筒を確認
        List<BlockState> validBarrels = new ArrayList<>();
        for (int barrelPos = 0; barrelPos < MAX_BARREL_LENGTH + 1; barrelPos++) {
            BlockState barrelState = level.getBlockState(worldPosition.above(barrelPos + 1));
            if(barrelState.getBlock() instanceof LensBarrelBlock){
                validBarrels.add(barrelState);
            }else{
                break;
            }
        }
        if(validBarrels.size() <= 1 || validBarrels.size() > MAX_BARREL_LENGTH){
            return false;
        }
        // 下端が主鏡か
        if(!validBarrels.get(0).is(TagRegistry.Blocks.MAIN_MIRROR)){
            return false;
        }
        // 上端が副鏡か
        if(!validBarrels.get(validBarrels.size() - 1).is(TagRegistry.Blocks.SECOND_MIRROR)){
            return false;
        }
        // 途中が筒か
        for (BlockState blockState : validBarrels.subList(1, validBarrels.size() - 1)) {
            if(blockState.is(TagRegistry.Blocks.MAIN_MIRROR) || blockState.is(TagRegistry.Blocks.SECOND_MIRROR)){
                return false;
            }
        }
        // 適用
        LensBarrelEntity barrelEntity = new LensBarrelEntity(level, worldPosition.above().getCenter(), validBarrels);//.create(level);
        level.addFreshEntity(barrelEntity);
        
        // 鏡筒のブロックステートをブロックエンティティに保存
        for (int barrelPos = 0; barrelPos < validBarrels.size(); barrelPos++) {
            level.setBlock(worldPosition.above(barrelPos + 1), Blocks.AIR.defaultBlockState(), 3);
        }
        // インターフェースブロックを設置
        level.setBlock(worldPosition, this.getBlockState().setValue(ConcentratorBlock.ENABLED, true), 3);
        this.setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 0.7f);
        level.playSound(null, worldPosition, SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.BLOCKS, 1.0f, 0.8f);
        return true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.heliopause.concentrator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        // 表示メニュー
        ConcentratorMenu menu = new ConcentratorMenu(containerId, playerInventory, this, this.data);
        return menu;
    }
    
    public void drops(){
        SimpleContainer inventory =new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, ConcentratorBlockEntity blockEntity){
        if(level.isClientSide()){
            return;
        }
        if(level instanceof ServerLevel serverLevel){
            date = Season.getDayInYear(serverLevel);
            dayTime = (long)(serverLevel.getTimeOfDay(1.0F) * 24000 + 6000);
            int seasonAngle = (int)Math.floor(((double) date /Season.YEAR_LENGTH) * 360.0);

            LensBarrelEntity lensBarrelEntity = null;
            for (LensBarrelEntity entity : level.getEntitiesOfClass(LensBarrelEntity.class, new AABB(worldPosition.above()))) {
                lensBarrelEntity = entity;
                break;
            }
            if(lensBarrelEntity != null) {
                isSyncedToStar = lensBarrelEntity.isSyncedToStar();
                barrelCoverage = lensBarrelEntity.getWholeCoverage();
                operateRecipe(serverLevel, barrelCoverage, seasonAngle);
            }
            operateTankInOut();
            
            this.setChanged();
            level.sendBlockUpdated(pos, blockState, blockState, 3);
        }
    }
    
    private void operateRecipe(ServerLevel level, Set<BarrelCoverageData> barrelCoverage, int seasonAngle){
        ItemStack itemIn = itemHandler.getStackInSlot(0);
        FluidStack fluidIn = fluidHandler.fluidTanks[0].getFluid();

        for (StarlightConcentrationRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeTypeRegistry.STARLIGHT_CONCENTRATION.get())) {

            // ingredient 判定
            if (!recipe.getIngredientItem().isEmpty()) {
                if(!recipe.getIngredientItem().test(itemIn)){
                    continue;
                }
            }
            if (!recipe.getIngredientFluid().isEmpty()) {
                if(!recipe.getIngredientFluid().isFluidEqual(fluidIn)){
                    continue;
                }
            }
            
            // coverage 判定
            if (!barrelCoverage.stream().map(BarrelCoverageData::name).collect(Collectors.toSet())
                    .equals(recipe.getConditions().coverage())) {
                continue;
            }

            // season 判定
            boolean seasonMatch = false;
            StarlightConcentrationRecipe.SeasonRange range = recipe.getConditions().seasonRange();
            if (seasonAngle >= range.start() && seasonAngle <= range.end()) {
                seasonMatch = true;
            }
            if (!seasonMatch) continue;

            // dimension 判定
            if (!level.dimension().location().toString().equals(recipe.getConditions().dimension())) {
                continue;
            }

            // time カウント
            if (currentRecipe == null || currentRecipe != recipe) {
                currentRecipe = recipe;
                maxProgress = recipe.getTime();
                progress = 0;
            }
            
            // 観測状況が有効ならレシピ進行
            if(isSyncedToStar) {
                progress++;
            }

            if (progress >= recipe.getTime()) {
                finishRecipe(recipe);
                progress = 0;
                currentRecipe = null;
            }

            return;
        }

        currentRecipe = null;
        progress = 0;
    }
    
    private void finishRecipe(StarlightConcentrationRecipe recipe){
        // 消費
        if (!recipe.getIngredientItem().isEmpty()) {
            itemHandler.extractItem(SLOT_INPUT_ITEM, 1, false);
        }
        if (!recipe.getIngredientFluid().isEmpty()) {
            FluidStack required = recipe.getIngredientFluid();
            fluidHandler.drainFrom(SLOT_INPUT_FLUID, required.getAmount(), FluidAction.EXECUTE);
        }
        // 追加
        if (!recipe.getResultItem(null).isEmpty()) {
            ItemStack result = recipe.getResultItem(null).copy();
            itemHandler.insertItem(SLOT_OUTPUT_ITEM, result, false);
        }
        if (!recipe.getResultFluid().isEmpty()) {
            FluidStack resultFluid = recipe.getResultFluid().copy();
            fluidHandler.fillTo(SLOT_OUTPUT_FLUID, resultFluid, FluidAction.EXECUTE);
        }
        this.setChanged();
    }
    
    private void operateTankInOut(){
        // プレイヤーごとに操作を見る
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof ConcentratorMenu) {
                ConcentratorMenu menu = (ConcentratorMenu) player.containerMenu;
                if (menu.blockEntity.getBlockPos().equals(this.getBlockPos())) {
                    handleTankIO(menu, SLOT_INPUT_FLUID, 0, 1, true);
                    handleTankIO(menu, SLOT_OUTPUT_FLUID, 2, 3, false);
                }
                menu.broadcastChanges();
            }
        }
    }
    
    public void handleTankIO(ConcentratorMenu menu, int tankId, int slotIn, int slotOut, boolean allowFill) {
        ItemStack container = menu.getGuiStack(slotIn);
        if (container.isEmpty()) {
            return;
        }
        // 出力スロットが埋まっている場合は処理しない
        if (!menu.getGuiStack(slotOut).isEmpty()) {
            return;
        }
        
        FluidUtil.getFluidHandler(container).ifPresent(handler -> {
            FluidStack tankFluid = fluidHandler.getFluidInTank(tankId);
            
            // 方向判定
            boolean containerHasFluid = allowFill && !handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE).isEmpty();
            
            // アイテムからタンク
            if (containerHasFluid) {
                FluidStack drainedSim = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                if (drainedSim.isEmpty()) {
                    return;
                }
                
                int fillSim = fluidHandler.fillTo(tankId, drainedSim, FluidAction.SIMULATE);
                if (fillSim <= 0) {
                    return;
                }
                
                // 実行
                FluidStack drained = handler.drain(fillSim, FluidAction.EXECUTE);
                fluidHandler.fillTo(tankId, drained, FluidAction.EXECUTE);
                
                ItemStack empty = handler.getContainer().copy();
                ItemStack remaining = container.copy();
                remaining.shrink(1);
                menu.setGuiStack(slotIn, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
                menu.setGuiStack(slotOut, empty);
                return;
            }
            // タンクから取り出し
            int fillSim = handler.fill(tankFluid, FluidAction.SIMULATE);
            if (fillSim <= 0) {
                return;
            }
            
            FluidStack drained = fluidHandler.drainFrom(tankId, fillSim, FluidAction.EXECUTE);
            handler.fill(drained, FluidAction.EXECUTE);
            ItemStack filled = handler.getContainer().copy();
            ItemStack remaining = container.copy();
            remaining.shrink(1);
            menu.setGuiStack(slotIn, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            menu.setGuiStack(slotOut, filled);
        });
    }
    
}
