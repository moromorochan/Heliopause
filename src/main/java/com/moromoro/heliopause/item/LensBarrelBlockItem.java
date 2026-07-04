package com.moromoro.heliopause.item;

import com.mojang.datafixers.util.Either;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.enumProperty.LensBarrelCoverageIconValue;
import com.moromoro.heliopause.implementable.IHasTooltipDraw;
import com.moromoro.heliopause.recipe.LensBarrelCoverageListener;
import com.moromoro.heliopause.tooltip.IconTooltipComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RenderTooltipEvent;

import java.util.List;

public class LensBarrelBlockItem extends BlockItem implements IHasTooltipDraw {
    private static final ResourceLocation COVERAGE_ICON = new ResourceLocation(Heliopause.MODID, "textures/gui/coverage_icon.png");
    public LensBarrelBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
    
    private static List<LensBarrelCoverageListener.BarrelCoverageData> getBarrelCoverage(Block block){
        String id = BuiltInRegistries.BLOCK.getKey(block).toString();
        return LensBarrelCoverageListener.DATA.values().stream().filter(data -> data.block().contains(id)).toList();
    }
    
    @Override
    public boolean renderTooltip(RenderTooltipEvent.GatherComponents event, ClientLevel level, ItemStack itemStack) {
        List<LensBarrelCoverageListener.BarrelCoverageData> barrelCoverage = getBarrelCoverage(getBlock());
        if (barrelCoverage.isEmpty()) {
            return false;
        }
        event.getTooltipElements().add(Either.left(Component.literal(Component.translatable("gui.heliopause.lens_barrel.coverage").getString()+":").withStyle(ChatFormatting.GRAY)));
        for (LensBarrelCoverageListener.BarrelCoverageData data : barrelCoverage) {
            
            String text = Component.translatable("gui.heliopause.lens_barrel_coverage." + data.name()).getString();
            LensBarrelCoverageIconValue icon = LensBarrelCoverageIconValue.fromString(data.icon());
            if(icon == null){
                continue;
            }
            int[] color = data.icon_color();
            IconTooltipComponent.IconData iconData =
                new IconTooltipComponent.IconData(COVERAGE_ICON, icon.ordinal() * 7,0,7,7, 32, 16, color);
            event.getTooltipElements().add(Either.right(new IconTooltipComponent(text, iconData)));
        }
        
        return true;
    }
}
