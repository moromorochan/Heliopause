package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.block.ConcentratorBlock;
import com.moromoro.heliopause.block.LensBarrelBlock;
import com.moromoro.heliopause.entity.LensBarrelEntity;
import com.moromoro.heliopause.generic.Season;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import com.moromoro.heliopause.screen.ConcentratorMenu;
import com.moromoro.heliopause.screen.ConcentratorScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class ConcentratorBlockEntity extends BlockEntity implements IFluidHandler, MenuProvider {
    private static final int MAX_BARREL_LENGTH = 4;
    //private int targetStarX;    // ターゲット赤経(α)
    //private int targetStarY;    // ターゲット赤緯(δ)
    private Vector2i targetStarCoordinate;
    private boolean isSyncedToStar;
    private boolean canSeeSky;

    public static final int STAR_COUNT = 150;  // 星の数
    public static final int STAR_RANGE_X = 360;
    public static final int STAR_RANGE_MIN_Y = -90;
    public static final int STAR_RANGE_MAX_Y = 90;
    // 表示メニュー
    private ConcentratorMenu menu;
    // メニュー渡し用データ ワールド依存なのでnbtには保存しない
    private long levelSeed;
    private long date;
    private long dayTime;

    // 内部アイテム(液体取り出し用スロット)
    private final ItemStackHandler itemHandler = new ItemStackHandler(3){
        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }
    };
    private static final int TANK_CAPACITY = 12000;
    private final FluidTank[] fluidTanks = new FluidTank[]{
        new FluidTank((TANK_CAPACITY)),
        new FluidTank((TANK_CAPACITY)),
        new FluidTank((TANK_CAPACITY))
    };

    // long値は上下に分割して送る
    public static final int DATA_ACCESS_LENGTH = 7;
    public static final int LEVEL_SEED_LOW = 0;
    public static final int LEVEL_SEED_HIGH = 1;
    public static final int DATE_LOW = 2;
    public static final int DATE_HIGH = 3;
    public static final int TIME_LOW = 4;
    public static final int TIME_HIGH = 5;
    public static final int COORDINATE = 6;
    //public static final int TRACK_Y = 7;

    // GUI用データ格納
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index){
                case LEVEL_SEED_LOW -> (int) (ConcentratorBlockEntity.this.levelSeed & 0xFFFFFFFFL);
                case LEVEL_SEED_HIGH -> (int) (ConcentratorBlockEntity.this.levelSeed >>> 32);
                case DATE_LOW -> (int) (ConcentratorBlockEntity.this.date & 0xFFFFFFFFL);
                case DATE_HIGH -> (int) (ConcentratorBlockEntity.this.date >>> 32);
                case TIME_LOW -> (int) (ConcentratorBlockEntity.this.dayTime & 0xFFFFFFFFL);
                case TIME_HIGH -> (int) (ConcentratorBlockEntity.this.dayTime >>> 32);
                case COORDINATE -> ConcentratorMenu.encodeStarCoordinate(targetStarCoordinate);
                //case TRACK_X -> targetStarCoordinate!=null ? targetStarCoordinate.x() : ConcentratorMenu.UNIDENTIFIED_COORDINATE;
                //case TRACK_Y -> targetStarCoordinate!=null ? targetStarCoordinate.y() : ConcentratorMenu.UNIDENTIFIED_COORDINATE;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case LEVEL_SEED_LOW -> {
                    long high = ConcentratorBlockEntity.this.levelSeed >>> 32;
                    ConcentratorBlockEntity.this.levelSeed = (high << 32) | (value & 0xFFFFFFFFL);
                }
                case LEVEL_SEED_HIGH -> {
                    long low = ConcentratorBlockEntity.this.levelSeed & 0xFFFFFFFFL;
                    ConcentratorBlockEntity.this.levelSeed = ((long) value << 32) | low;
                }
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
                case COORDINATE -> {
                    ConcentratorBlockEntity.this.targetStarCoordinate = ConcentratorMenu.decodeStarCoordinate(value);
                }
                /*case TRACK_X -> {
                    if(value == ConcentratorMenu.UNIDENTIFIED_COORDINATE){
                        ConcentratorBlockEntity.this.targetStarCoordinate = null;
                    } else if(targetStarCoordinate == null){
                        ConcentratorBlockEntity.this.targetStarCoordinate = new Vector2i(value,0);
                    } else{
                        ConcentratorBlockEntity.this.targetStarCoordinate.x = value;
                    }
                }
                case TRACK_Y -> {
                    if(value == ConcentratorMenu.UNIDENTIFIED_COORDINATE - 90){
                        ConcentratorBlockEntity.this.targetStarCoordinate = null;
                    } else if(targetStarCoordinate == null){
                        ConcentratorBlockEntity.this.targetStarCoordinate = new Vector2i(0, value);
                    } else{
                        ConcentratorBlockEntity.this.targetStarCoordinate.y = value;
                    }
                }*/
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
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("isTracking", targetStarCoordinate != null);
        if(targetStarCoordinate != null){
            nbt.putInt("tracking_x", targetStarCoordinate.x());
            nbt.putInt("tracking_y", targetStarCoordinate.y());
        }
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        if(nbt.getBoolean("isTracking")){
            targetStarCoordinate = new Vector2i(nbt.getInt("tracking_x"), nbt.getInt("tracking_y"));
        }else{
            targetStarCoordinate = null;
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

    /*public double getYaw() {
        return this.yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return this.pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }*/

    /*public List<BlockState> getBarrels() {
        return barrels;
    }*/

    public boolean assemble(){
        if(level == null){
            return false;
        }
        if(level.isClientSide()){
            return true;
        }
        // 下が魔法陣か確認
        if(!(level.getBlockEntity(worldPosition.below()) instanceof AltAzimuthCircleBoardBlockEntity)){
            return false;
        }
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
        // 適用
        LensBarrelEntity barrelEntity = new LensBarrelEntity(level, worldPosition.above().getCenter(), validBarrels);//.create(level);
        //barrelEntity.setPos(worldPosition.getCenter());
        //barrelEntity.assemble(validBarrels);
        level.addFreshEntity(barrelEntity);

        //this.barrels.clear();
        //this.barrels.addAll(validBarrels);
        // 鏡筒のブロックステートをブロックエンティティに保存
        for (int barrelPos = 0; barrelPos < validBarrels.size(); barrelPos++) {
            level.setBlock(worldPosition.above(barrelPos + 1), Blocks.AIR.defaultBlockState(), 3);
        }
        // インターフェースブロックを設置
        //level.setBlock(worldPosition.above(), BlockRegistry.STARLIGHT_CONCENTRATOR_INTERFACE.get().defaultBlockState(), 3);
        // enabled切り替え
        level.setBlock(worldPosition, this.getBlockState().setValue(ConcentratorBlock.ENABLED, true), 3);
        this.setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 0.8f);
        return true;
    }

    public void disAssemble(List<BlockState> barrels){
        if(level == null || level.isClientSide){
            return;
        }
        // enabled切り替え
        if(level.getBlockState(worldPosition).equals(this.getBlockState())) {
            level.setBlock(worldPosition, this.getBlockState().setValue(ConcentratorBlock.ENABLED, false), 3);
        }
        // ドロップ用
        LootParams.Builder lootBuilder = new LootParams.Builder((ServerLevel) level)
            .withParameter(LootContextParams.ORIGIN, worldPosition.getCenter())
            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        // 鏡筒をブロックに戻す 設置できない場合はドロップ
        for (int barrelPos = 0; barrelPos < barrels.size(); barrelPos++) {
            BlockState barrelState = barrels.get(barrelPos);
            BlockPos placePos = worldPosition.above(barrelPos + 1);
            if(level.getBlockState(placePos).canBeReplaced()) {
                level.setBlock(placePos, barrelState, 3);
            }else {
                List<ItemStack> drops = barrelState.getDrops(lootBuilder);
                SoundEvent breakSound = barrelState.getSoundType().getBreakSound();
                level.playSound(null, worldPosition, breakSound, SoundSource.BLOCKS);
                for (ItemStack drop : drops) {
                    ItemEntity itemEntity = new ItemEntity(level, placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5, drop);
                    level.addFreshEntity(itemEntity);
                }
            }
        }
        barrels.clear();
        this.setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 0.8f);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.heliopause.concentrator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        menu = new ConcentratorMenu(containerId, playerInventory, this, this.data);
        return menu;
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, ConcentratorBlockEntity blockEntity){
        if(level.isClientSide()){
            return;
        }
        if(level instanceof ServerLevel serverLevel){
            levelSeed = serverLevel.getSeed();
            date = Season.getDayInYear(serverLevel);
            dayTime = serverLevel.getDayTime();
            this.setChanged();
            level.sendBlockUpdated(pos, blockState, blockState, 3);
        }
        //blockEntity.setChanged();
        //level.sendBlockUpdated(pos, blockState, blockState, 3);

        // 日時から星の角度を取得
        double yearAngle = (double) Season.getDayInYear((ServerLevel) level) * 360 / Season.YEAR_LENGTH;
        double dayAngle = SiderostatBlockEntity.getCurrentMoonAngle(level);

        double starAngle = dayAngle + yearAngle;

        // 当たり判定用エンティティの制御

    }

    public boolean isSyncedToStar() {
        return isSyncedToStar;
    }

    public void setSyncedToStar(boolean syncedToStar) {
        this.isSyncedToStar = syncedToStar;
    }

    public Vector2i getTargetStarCoordinate() {
        return targetStarCoordinate;
    }

    public void setTargetStarCoordinate(Vector2i targetStarCoordinate) {
        this.targetStarCoordinate = targetStarCoordinate;
    }

    @Override
    public int getTanks() {
        return fluidTanks.length;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return fluidTanks[tank].getFluidInTank(0);
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidTanks[tank].getTankCapacity(0);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return fluidTanks[tank].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        for (FluidTank fluidTank : fluidTanks) {
           int amount = fluidTank.fill(resource, action);
           if(amount > 0){
               return amount;
           }
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
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
        for (FluidTank fluidTank : fluidTanks) {
            FluidStack drained = fluidTank.drain(maxDrain, action);
            if(!drained.isEmpty()){
                return drained;
            }
        }
        return FluidStack.EMPTY;
    }
}
