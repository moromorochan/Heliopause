package com.moromoro.heliopause.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

//周転するリングの表現に使用するパーティクル
public class WhirlRingParticles extends TextureSheetParticle {

    protected float orbitAxisPitch;//回転軸のピッチ, 軌道系射角
    protected float orbitAxisYaw;//回転軸のヨー, 軌道傾斜の方角
    protected float orbitRotation;//回転のロール, 回転のオフセット
    protected float orbitRadius;//軌道円の大きさ, 軌道半径
    protected float centripetalForce;//向心力, 軌道速度を求めるのに必要
    protected float axisZError;

    protected int lightIntensity;//パーティクルの明るさ, 影を受けない値

    private float cameraDistance;//カメラからの距離, 小さくて遠ければパーティクルを消す

    protected WhirlRingParticles(ClientLevel clientLevel, double posX, double posY, double posZ, double velocityX, double velocityY, double velocityZ, SpriteSet spriteSet) {
        super(clientLevel, posX, posY, posZ, velocityX, velocityY, velocityZ);

        this.lifetime = 90;
        cameraDistance=0;
        lightIntensity=0;

        axisZError= (RandomSource.create().nextFloat()-0.5f)*0.037f;

        //アルファとテクスチャの読み込み
        this.setAlpha(0.0F);
        this.setSpriteFromAge(spriteSet);
        //元サイズ設定
        this.quadSize=4f;

        //軌道要素の初期化
        this.orbitAxisPitch = (float) Math.toRadians(-90);
        this.orbitAxisYaw = 0f;
        this.orbitRotation = 0f;
        this.orbitRadius = 2.0f;
        this.centripetalForce = 0.1f;

        //物理は無し 見た目の位置と実際の位置も違うので、当たり判定はブロックエンティティ側に任せる
        this.hasPhysics = false;

        //速度ベクトルを読み込み
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
    }

    //軌道要素を設定する
    public void setOrbitalElements(Vec3 axisPos, float axisPitch, float axisYaw, float axisRoll, float radius, float centripetalForce){
        this.orbitAxisPitch = axisPitch;
        this.orbitAxisYaw = axisYaw;
        this.orbitRotation = axisRoll;
        this.orbitRadius = radius;
        this.centripetalForce = centripetalForce / 1000f;
    }

    public void setPixelBasedSize(float quadPixelSize){
        this.quadSize= Math.sqrt(Math.ceil(quadPixelSize*16f))/4f;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {

        //カメラ位置を取得
        Vec3 cameraPos = camera.getPosition();

        //tick前の位置とtick後の位置を補完 & カメラからの相対座標に変換
        float partialPosX = (float)(Mth.lerp((double)partialTicks, this.xo, this.x) - cameraPos.x());
        float partialPosY = (float)(Mth.lerp((double)partialTicks, this.yo, this.y) - cameraPos.y());
        float partialPosZ = (float)(Mth.lerp((double)partialTicks, this.zo, this.z) - cameraPos.z());

        //軌道平面を取得
        Quaternionf orbitalPlane = getOrbitalPlane(partialTicks);

        //平面上にメッシュを配置するための座標を用意
        float quadHalf = this.quadSize/2f;
        float quadLengthHalf = (quadHalf + (0.5f*Math.min(1/quadHalf,this.orbitRadius))) * this.alpha;
        float radius = this.orbitRadius/quadSize;
        float zError = -0.02f*quadSize;
        //オモテ用
        Vector3f [] overVertexPosArray = new Vector3f[]{
                new Vector3f(-quadLengthHalf, -quadHalf + radius, axisZError-zError),
                new Vector3f(-quadLengthHalf, quadHalf + radius, axisZError-zError),
                new Vector3f(quadLengthHalf, quadHalf + radius, axisZError-zError),
                new Vector3f(quadLengthHalf, -quadHalf + radius, axisZError-zError)
        };
        //ウラ用
        Vector3f [] underVertexPosArray = new Vector3f[]{
                new Vector3f(-quadLengthHalf, -quadHalf + radius, axisZError+zError),
                new Vector3f(-quadLengthHalf, quadHalf + radius, axisZError+zError),
                new Vector3f(quadLengthHalf, quadHalf + radius, axisZError+zError),
                new Vector3f(quadLengthHalf, -quadHalf + radius, axisZError+zError)
        };

        for(int i = 0; i < 4; ++i) {
            Vector3f overMeshVector = overVertexPosArray[i];
            overMeshVector.rotate(orbitalPlane);
            overMeshVector.mul(quadSize);
            overMeshVector.add(partialPosX, partialPosY, partialPosZ);

            Vector3f underMeshVector = underVertexPosArray[i];
            underMeshVector.rotate(orbitalPlane);
            underMeshVector.mul(quadSize);
            underMeshVector.add(partialPosX,partialPosY,partialPosZ);
        }

        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int lightColor = calcLight(this.getLightColor(partialTicks),lightIntensity);//lightIntensity;//Math.max(lightIntensity, this.getLightColor(partialTicks));
        //オモテ面
        buffer.vertex((double)overVertexPosArray[0].x(), (double)overVertexPosArray[0].y(), (double)overVertexPosArray[0].z()).uv(maxU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        buffer.vertex((double)overVertexPosArray[1].x(), (double)overVertexPosArray[1].y(), (double)overVertexPosArray[1].z()).uv(maxU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        buffer.vertex((double)overVertexPosArray[2].x(), (double)overVertexPosArray[2].y(), (double)overVertexPosArray[2].z()).uv(minU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        buffer.vertex((double)overVertexPosArray[3].x(), (double)overVertexPosArray[3].y(), (double)overVertexPosArray[3].z()).uv(minU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        //ウラ面
        buffer.vertex((double)underVertexPosArray[0].x(), (double)underVertexPosArray[0].y(), (double)underVertexPosArray[0].z()).uv(maxU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        buffer.vertex((double)underVertexPosArray[3].x(), (double)underVertexPosArray[3].y(), (double)underVertexPosArray[3].z()).uv(minU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        buffer.vertex((double)underVertexPosArray[2].x(), (double)underVertexPosArray[2].y(), (double)underVertexPosArray[2].z()).uv(minU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();
        buffer.vertex((double)underVertexPosArray[1].x(), (double)underVertexPosArray[1].y(), (double)underVertexPosArray[1].z()).uv(maxU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(lightColor).endVertex();

        cameraDistance = Vector3f.length(overVertexPosArray[0].x(),overVertexPosArray[0].y(),overVertexPosArray[0].z());
    }

    private Quaternionf getOrbitalPlane(float partialTicks) {
        Quaternionf orbitalPlane = new Quaternionf();
        orbitalPlane.rotateZ(orbitAxisYaw);
        orbitalPlane.rotateX(orbitAxisPitch);
        //回転
        orbitalPlane.rotateZ(orbitRotation + Mth.lerp(partialTicks,0,getOrbitSpeed()));
        return orbitalPlane;
    }

    //TODO バウンディングボックスの最適化(今は半径を拡げて無理矢理設定している)
    public AABB getBoundingBox() {
        Vector3f basePos = new Vector3f(0,1600*orbitRadius*quadSize,0);
        Quaternionf orbitalPlane = getOrbitalPlane(0);
        basePos.rotate(orbitalPlane);
        basePos.mul(quadSize);
        basePos.add(new Vector3f((float) this.x, (float) this.y, (float) this.z));

        float quadHalf = this.quadSize/2f;
        float quadLengthHalf = (quadHalf + (0.5f*Math.min(1/quadHalf,this.orbitRadius))) * this.alpha;

        return new AABB(
                this.x - orbitRadius*2, this.y - orbitRadius*2, this.z - 0.1f,
                this.x + orbitRadius*2, this.y + orbitRadius*2, this.z + 0.1f
        );
    }

    @Override
    public void tick() {
        super.tick();

        //小さくて遠いなら消す
        if((this.cameraDistance/ java.lang.Math.pow(this.quadSize,0.2f))>80){
            this.remove();
        }
        //寿命に応じてアルファを設定 アルファは幅に使用する
        if(this.age<30){
            this.alpha=age/30f;
        } else if (this.age<60) {
            this.alpha=1.0f;
        }else{
            this.alpha=(90-age)/30f;
        }

        if (!this.removed) {
            //回転
            this.orbitRotation += getOrbitSpeed();

            //
        }
    }
    //半径と向心力に基づいた軌道速度を出す
    private float getOrbitSpeed() {
        return Mth.sqrt(centripetalForce/orbitRadius);
    }

    public void setLightIntensity(int intensity){
        this.lightIntensity=intensity;
    }

    //ブロックの光レベルの取得
    private static int calcLight(int combinedLight, int Blocklight){
        int skyLight = combinedLight >> 20 & 15;
        int blockLight = combinedLight >> 4 & 15;
        //計算
        int maxBlockLight = Math.max(blockLight, Blocklight);
        return (skyLight << 20| maxBlockLight << 4);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprites) {
            this.spriteSet = sprites;
        }

        public Particle createParticle(@NotNull SimpleParticleType particleType, @NotNull ClientLevel level, double posX, double posY, double posZ, double velocityX, double velocityY, double velocityZ) {
            return new WhirlRingParticles(level, posX, posY, posZ, velocityX, velocityY, velocityZ, this.spriteSet);
        }
    }
}
