package com.moromoro.heliopause.entity;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.FluidSpreaderOrbBlockEntity;
import com.moromoro.heliopause.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

//インタラクト用エンティティクラスを宣言
public class OrreryInteractionOperatorEntity extends Entity {

    private BlockPos blockStatePos;
    private UUID playerUUID;
    //private ServerLevel level;
    private int updateTick;//操作が無くなったときの削除用タイマー

    public OrreryInteractionOperatorEntity(EntityType<? extends Entity> entityType, Level level) {
        super(EntityRegistry.ORRERY_INTERACTION_OPERATOR_E.get(), level);
        this.blockStatePos=null;
        this.playerUUID=null;
        //if(!level.isClientSide()) {this.level = (ServerLevel) level;}
        this.updateTick = 0;
    }

    public OrreryInteractionOperatorEntity(Level level, BlockPos blockStatePos, UUID playerUUID){
        super(EntityRegistry.ORRERY_INTERACTION_OPERATOR_E.get(), level);
        this.blockStatePos = blockStatePos;
        //this.setPos(position);
        this.playerUUID = playerUUID;
        //if(!level.isClientSide()) {this.level = (ServerLevel) level;}
        this.updateTick = 0;
        //tick();
    }

    @Override
    public void tick(){
        if(!this.level().isClientSide()){
            ServerLevel serverLevel = (ServerLevel) this.level();
            if(this.playerUUID!=null && (serverLevel.getBlockEntity(this.blockStatePos) instanceof FluidSpreaderOrbBlockEntity)) {
            Player player = serverLevel.getPlayerByUUID(this.playerUUID);
            if (player == null
                || this.position().distanceTo(player.getEyePosition()) > 2
                || this.position().distanceTo(this.blockStatePos.getCenter()) > 5) {
                this.discard();
            }else{
                //位置を設定
                updatePos(serverLevel,this.playerUUID);
                //削除タイマー
                this.updateTick++;
                if(this.updateTick > 10){
                    this.discard();
                }
            }
        }else{
            this.discard();
        }
        }
    }
    //押しのけ判定は常にオフ
    @Override public boolean canBeCollidedWith() { return false; }

    // インタラクト可能であることを示す
    @Override public boolean isPickable() { return true; }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand){
        if (!this.level().isClientSide) {
            //右クリックされたとき
            Heliopause.LOGGER.debug("right clicked!");
            if(this.level().getBlockEntity(this.blockStatePos) instanceof FluidSpreaderOrbBlockEntity entity){
                entity.setCircumstellars(player, hand, 0);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount){
        if (!this.level().isClientSide) {
            //左クリックされたとき
            Heliopause.LOGGER.debug("left clicked!");
            if(this.level().getBlockEntity(this.blockStatePos) instanceof FluidSpreaderOrbBlockEntity entity){
                entity.setCircumstellars((Player) source.getEntity(), InteractionHand.MAIN_HAND, 1);
            }
        }
        return true;
    }

    private void updatePos(ServerLevel level, UUID playerUUID){
        //視線を取得
        //BlockPos blockStatePos = this.getBlockPos();
        Player player = this.level().getPlayerByUUID(playerUUID);
        Vec3 playerLookVec = player.getLookAngle();
        Vec3 playerEyePos = player.getEyePosition();

        //軌道平面との交点を計算
        double playerToCursorDist = (blockStatePos.getCenter().y - playerEyePos.y) / playerLookVec.y;
        //カーソルが2ブロック以上離れているか、頭の反対側ならキャンセル
        if(playerToCursorDist < 0d || playerToCursorDist > 2d){this.discard();}
        Vec3 intersection = playerEyePos.add(playerLookVec.scale(playerToCursorDist));
        this.setPos(intersection.add(0,-0.1f,0));
    }

    public BlockPos getBlockStatePos() {
        return blockStatePos;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void notifyUpdate() {
        this.updateTick = 0;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.blockStatePos = new BlockPos(nbt.getInt("BlockPosX"),nbt.getInt("BlockPosY"),nbt.getInt("BlockPosZ"));
        this.playerUUID = nbt.getUUID("PlayerUUID");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("BlockPosX", this.blockStatePos.getX());
        nbt.putInt("BlockPosY", this.blockStatePos.getY());
        nbt.putInt("BlockPosZ", this.blockStatePos.getZ());
        nbt.putUUID("PlayerUUID", this.playerUUID);
    }
}