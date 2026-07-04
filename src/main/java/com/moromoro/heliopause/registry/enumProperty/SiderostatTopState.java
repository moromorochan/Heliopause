package com.moromoro.heliopause.registry.enumProperty;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum SiderostatTopState implements StringRepresentable {
    FULL,
    MOVING,
    EMPTY;

    public static EnumProperty<SiderostatTopState> create(String type, Class<SiderostatTopState> writtenBoardDrawTypeClass){
        return EnumProperty.create(type, writtenBoardDrawTypeClass);
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
