package com.moromoro.heliopause.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public class StarRippleParticles extends TextureSheetParticle {
    //private static final int spriteFrameCount = 10;
    private final SpriteSet spriteSet;
    private final float randomAngle;
    protected StarRippleParticles(ClientLevel clientLevel, double posX, double posY, double posZ, double velocityX, double velocityY, double velocityZ, SpriteSet spriteSet) {
        super(clientLevel, posX, posY, posZ, velocityX, velocityY, velocityZ);
        this.spriteSet = spriteSet;
        this.lifetime = 35;
        this.setParticleSpeed(0,0,0);
        this.quadSize = 0.8f;//this.random.nextInt(5,8) * 0.1f;
        // 最初を取得
        this.setSprite(spriteSet.get(0,1));
        //this.setSprite(spriteSet.get(0,spriteFrameCount));
        this.randomAngle = (float) (this.random.nextFloat()*Math.PI*2);
    }

    @Override
    public void render(VertexConsumer p_107678_, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float f = (float)(Mth.lerp((double)partialTicks, this.xo, this.x) - cameraPos.x());
        float f1 = (float)(Mth.lerp((double)partialTicks, this.yo, this.y) - cameraPos.y());
        float f2 = (float)(Mth.lerp((double)partialTicks, this.zo, this.z) - cameraPos.z());

        Vector3f[] meshVector = new Vector3f[]{new Vector3f(-1.0F, 0.0F, -1.0F), new Vector3f(-1.0F, 0.0F, 1.0F), new Vector3f(1.0F, 0.0F, 1.0F), new Vector3f(1.0F, 0.0F, -1.0F)};
        float f3 = this.getQuadSize(partialTicks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = meshVector[i];
            vector3f.rotateY(randomAngle);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        float f6 = this.getU0();
        float f7 = this.getU1();
        float f4 = this.getV0();
        float f5 = this.getV1();
        int j = this.getLightColor(partialTicks);
        // オモテ
        p_107678_.vertex(meshVector[0].x(), meshVector[0].y(), meshVector[0].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        p_107678_.vertex(meshVector[1].x(), meshVector[1].y(), meshVector[1].z()).uv(f7, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        p_107678_.vertex(meshVector[2].x(), meshVector[2].y(), meshVector[2].z()).uv(f6, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        p_107678_.vertex(meshVector[3].x(), meshVector[3].y(), meshVector[3].z()).uv(f6, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();

        //ウラ
        p_107678_.vertex(meshVector[0].x(), meshVector[0].y(), meshVector[0].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        p_107678_.vertex(meshVector[3].x(), meshVector[3].y(), meshVector[3].z()).uv(f7, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        p_107678_.vertex(meshVector[2].x(), meshVector[2].y(), meshVector[2].z()).uv(f6, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        p_107678_.vertex(meshVector[1].x(), meshVector[1].y(), meshVector[1].z()).uv(f6, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();

    }

    @Override
    public void tick() {
        super.tick();
        this.setSprite(spriteSet.get(this.age * 2,this.lifetime + this.age));
        this.quadSize *= 1.01f;
        this.alpha = (this.lifetime - this.age) / (float) this.lifetime;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void setScale(float scale) {
        this.quadSize = scale;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprites) {
            this.spriteSet = sprites;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double posX, double posY, double posZ, double velocityX, double velocityY, double velocityZ) {
            return new StarRippleParticles(level, posX, posY, posZ, velocityX, velocityY, velocityZ, this.spriteSet);
        }
    }
}
