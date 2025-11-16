package com.moromoro.heliopause.blockStateProperty;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum WrittenBoardType implements StringRepresentable {
    BLANK_CIRCLE,
    DOUBLE_CIRCLE,
    CROSS_CIRCLE,
    SQUARE,
    DIAMOND,
    DIAMOND_STAR;

    public static EnumProperty<WrittenBoardType> create(String type, Class<WrittenBoardType> writtenBoardTypeClass){
        return EnumProperty.create(type, writtenBoardTypeClass);
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
