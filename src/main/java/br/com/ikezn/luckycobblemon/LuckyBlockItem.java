package br.com.ikezn.luckycobblemon;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class LuckyBlockItem extends BlockItem {
    public LuckyBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static int getLuck(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(LuckyCobblemonMod.LUCK_COMPONENT, 0), -100, 100);
    }

    public static void setLuck(ItemStack stack, int luck) {
        int clamped = Math.clamp(luck, -100, 100);
        if (clamped == 0) {
            stack.remove(LuckyCobblemonMod.LUCK_COMPONENT);
        } else {
            stack.set(LuckyCobblemonMod.LUCK_COMPONENT, clamped);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        int luck = getLuck(stack);
        Formatting color = luck > 0 ? Formatting.GREEN : luck < 0 ? Formatting.RED : Formatting.GRAY;
        String prefix = luck > 0 ? "+" : "";
        tooltip.add(Text.literal("Sorte: " + prefix + luck).formatted(color));
        tooltip.add(Text.literal("Combine com minerais na bancada").formatted(Formatting.DARK_GRAY));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return getLuck(stack) > 0 || super.hasGlint(stack);
    }
}
