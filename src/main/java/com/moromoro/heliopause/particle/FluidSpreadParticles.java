package com.moromoro.heliopause.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FluidSpreadParticles extends TextureSheetParticle {

    protected FluidSpreadParticles(ClientLevel clientLevel, double posX, double posY, double posZ, double velocityX, double velocityY, double velocityZ, SpriteSet spriteSet) {
        super(clientLevel, posX, posY, posZ, velocityX, velocityY, velocityZ);

        this.setAlpha(0.0F);
        this.setSpriteFromAge(spriteSet);

        this.lifetime = 48+ RandomSource.create().nextInt(0,12);

        this.hasPhysics = true;
        this.friction = 0.95F;
        this.quadSize = 0.2F;
        this.gravity = 1F;

        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        //速度ベクトルを取得
        Vec3 velocity = getVelocity();
        //カメラ位置を取得
        Vec3 cameraPos = camera.getPosition();
        //tick前の位置とtick後の位置を補完 & カメラからの相対座標に変換
        float partialPosX = (float)(Mth.lerp((double)partialTicks, this.xo, this.x) - cameraPos.x());
        float partialPosY = (float)(Mth.lerp((double)partialTicks, this.yo, this.y) - cameraPos.y());
        float partialPosZ = (float)(Mth.lerp((double)partialTicks, this.zo, this.z) - cameraPos.z());

        //カメラ基準の角度を計算
        float horizontalCamAngle = (float) Math.toRadians(camera.getYRot());//
        float verticalCamAngle = (float) Math.toRadians(camera.getXRot());//(Math.atan2(new Vector2f(partialPosX,partialPosZ).length(),partialPosY));

        //位置毎の角度を計算
        float horizontalPartialAngle = (float) Math.atan2(partialPosX,partialPosZ);
        float verticalPartialAngle = (float) Math.atan2(new Vector2f(partialPosX,partialPosZ).length(),partialPosY);

        //速度ベクトルを水平と垂直に分解
            //タテ
        float horizontalVelAngle = (float) Math.atan2(velocity.x,velocity.z);

        //横方向の速度を符号付きで取得
        float horizontalVelocity = (float) (new Vector2f((float) velocity.x, (float) velocity.z).length() * Math.signum(velocity.x * velocity.z));
            //ヨコ
        float verticalVelAngle = (float) (Math.atan2(horizontalVelocity, velocity.y));

        //合成した角度を用意
        float horizontalCombinedAngle = horizontalCamAngle - horizontalVelAngle;
        float verticalCombinedAngle = verticalCamAngle - verticalVelAngle;

        //float horizontalCombinedAngle = (float) (Math.cos(verticalCamAngle)*horizontalCamAngle + Math.sin(verticalCamAngle)*horizontalVelAngle);

        Quaternionf faceRotation = new Quaternionf();
        //面の向きをカメラに合わせる
        faceRotation.rotateY(horizontalPartialAngle);
        faceRotation.rotateX(verticalCamAngle);

        //速度ベクトルをスクリーンに投影
        //Vector3f billboardVelocity = velocity.toVector3f().rotate(camera.rotation());
        //float horizontalZ =(float) (horizontalCombinedAngle * Math.sin(verticalCamAngle));
        float verticalZ = (float) (Math.atan2(Math.cos(horizontalCombinedAngle) * -horizontalVelocity, velocity.y));
        //float combinedZ = horizontalZ+verticalZ;
        faceRotation.rotateZ(verticalZ);

        //面とスクリーンのなす角を取得
        float billboardVerticalAngle = verticalPartialAngle + verticalVelAngle + (float) (Math.PI/2);

        //頂点の座標を用意
        float dropWidth =(Math.max(0.5f,1f/(float) Math.max(1.0f, Math.min(5.0f,Math.pow(velocity.length(),2.2f) * 22f ))));
        float dropLength = (float)(
                 Math.abs(Math.sin(billboardVerticalAngle)) * dropWidth
                +Math.abs(Math.cos(billboardVerticalAngle))* Math.max(1.0f, Math.min(5.0f,Math.pow(velocity.length(),2.2f) * 22f ))
        );

        Vector3f[] vertexPosArray = new Vector3f[]{
                new Vector3f(-dropWidth, -dropLength, 0.0F),
                new Vector3f(-dropWidth, dropLength, 0.0F),
                new Vector3f(dropWidth, dropLength, 0.0F),
                new Vector3f(dropWidth, -dropLength, 0.0F)
        };
        //パーティクルの大きさを取得
        float quadSize = this.getQuadSize(partialTicks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = vertexPosArray[i];
            vector3f.rotate(faceRotation);
            vector3f.mul(quadSize);
            vector3f.add(partialPosX, partialPosY, partialPosZ);
        }

        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int j = this.getLightColor(partialTicks);
        buffer.vertex(vertexPosArray[0].x(), vertexPosArray[0].y(), vertexPosArray[0].z()).uv(maxU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(vertexPosArray[1].x(), vertexPosArray[1].y(), vertexPosArray[1].z()).uv(maxU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(vertexPosArray[2].x(), vertexPosArray[2].y(), vertexPosArray[2].z()).uv(minU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(vertexPosArray[3].x(), vertexPosArray[3].y(), vertexPosArray[3].z()).uv(minU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
    }

    private Vec3 getVelocity() {
        return new Vec3(xd,yd,zd);
    }
/*
    private void getDirectionTowards(Quaternionf quaternionf, float horizontalAngle, float verticalAngle, Vec3 direction) {
        //座標系を回転
        Vector3f localDirection = direction.toVector3f().rotateX(horizontalAngle);

        //オイラー角へ変換
        float rotation = (float) (
                Math.atan2(
                        new Vector2f(localDirection.x,localDirection.z).length(),
                        localDirection.y
                )
        )* -Math.signum(localDirection.x*localDirection.z);

        //四元数へ変換
        quaternionf.rotateZ(rotation);
    }
*/
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        RandomSource randomSource = RandomSource.createNewThreadLocalInstance();
        super.tick();
        if(this.age==1){
            setAlpha(1.0f);
            if(randomSource.nextDouble()<8d/100d)
                level.playLocalSound(this.x, this.y, this.z, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.15F, 1.0F, true);
        }
        if (!this.removed) {

            this.xd *= 0.9d;
            this.zd *= 0.9d;
            if (this.age > this.lifetime / 2) {
                this.setAlpha(1.0F - ((float)this.age) / (float)this.lifetime*2);
                friction -= 0.015f;
                this.xd -= (double) ((float)this.age - (float)(this.lifetime / 2)) * 0.003F * (0.2-Math.random());
                this.zd -= (double) ((float)this.age - (float)(this.lifetime / 2)) * 0.003F * (0.2-Math.random());
            }
            else if (this.onGround) {
                BlockState blockState = level.getBlockState(new BlockPos((int) Math.floor(this.getPos().x), (int) Math.floor(this.getPos().y-0.1f), (int) Math.floor(this.getPos().z)));
                if(!blockState.isAir()) {
                    if(randomSource.nextDouble()<12d/100d)
                        level.playLocalSound(this.x, this.y, this.z, blockState.getSoundType().getFallSound(), SoundSource.BLOCKS, 0.05F, 1.0F, true);
                    if(randomSource.nextDouble()<8d/100d)
                        level.playLocalSound(this.x, this.y, this.z, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.15F, 1.0F, true);
                }
            }
            if(this.onGround){
                if(Math.abs(this.yd) > 0.2f){
                    this.yd = Math.signum(-this.yd)*Math.min(0.2f, Math.abs(this.yd) * 0.3f);
                    if(randomSource.nextFloat()<90d/100d)
                    {
                        this.lifetime = Math.min(this.lifetime,25);
                    }
                }else{
                    this.yd = 0f;
                    this.lifetime = Math.min(this.lifetime,25);
                }
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprites) {
            this.spriteSet = sprites;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double posX, double posY, double posZ, double velocityX, double velocityY, double velocityZ) {
            return new FluidSpreadParticles(level, posX, posY, posZ, velocityX, velocityY, velocityZ, this.spriteSet);
        }
    }
}