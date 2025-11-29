package com.moromoro.heliopause.EnumProperty;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum WrittenBoardNodeType implements StringRepresentable {
    ROOT,           // 起点の親
    CIRCUMFERENCE,  // 円周上のノード
    VERTEX;         // 線分の端点

    public static EnumProperty<WrittenBoardNodeType> create(String nodeType, Class<WrittenBoardNodeType> writtenBoardNodeTypeClass){
        return EnumProperty.create(nodeType, writtenBoardNodeTypeClass);
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
