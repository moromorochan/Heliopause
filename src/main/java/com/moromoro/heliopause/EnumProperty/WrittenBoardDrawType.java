package com.moromoro.heliopause.EnumProperty;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum WrittenBoardDrawType implements StringRepresentable {
    BLANK_CIRCLE,
    DOUBLE_CIRCLE,
    CROSS_CIRCLE,
    SQUARE,
    DIAMOND,
    DIAMOND_STAR;

    public static EnumProperty<WrittenBoardDrawType> create(String drawType, Class<WrittenBoardDrawType> writtenBoardDrawTypeClass){
        return EnumProperty.create(drawType, writtenBoardDrawTypeClass);
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
