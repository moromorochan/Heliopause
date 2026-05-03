package com.moromoro.heliopause.entity;

import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.registry.EntityRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class IngredientTransporterEntity extends StellarIngredientEntity{
    public IngredientTransporterEntity(EntityType<? extends Entity> entityType, Level level) {
        super(EntityRegistry.INGREDIENT_TRANSPORTER_E.get(), level);
    }
    
    public IngredientTransporterEntity(Level level, Vec3 pos, Vec3 velocity, CircumstellarIngredient ingredient) {
        super(EntityRegistry.INGREDIENT_TRANSPORTER_E.get(), level);
        this.setPos(pos.x(),pos.y(),pos.z());
        this.velocity = velocity;
        setDeltaMovement(velocity);
        this.setStellarStack(ingredient.getStellarStack());
        this.noPhysics = false;
        this.setNoGravity(true);
    }
    
    @Override
    public void countAge(){
        super.countAge();
    }
    
    @Override
    public void updateVelocity(){
        super.updateVelocity();
    }
}
