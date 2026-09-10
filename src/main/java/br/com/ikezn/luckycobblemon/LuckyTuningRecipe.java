package br.com.ikezn.luckycobblemon;

import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public final class LuckyTuningRecipe extends SpecialCraftingRecipe {
    private static final Map<Item, Integer> MODIFIERS = Map.ofEntries(
        Map.entry(Items.IRON_INGOT, 3),
        Map.entry(Items.IRON_BLOCK, 30),
        Map.entry(Items.GOLD_INGOT, 6),
        Map.entry(Items.GOLD_BLOCK, 60),
        Map.entry(Items.EMERALD, 8),
        Map.entry(Items.EMERALD_BLOCK, 80),
        Map.entry(Items.DIAMOND, 12),
        Map.entry(Items.DIAMOND_BLOCK, 100),
        Map.entry(Items.GOLDEN_APPLE, 40),
        Map.entry(Items.ENCHANTED_GOLDEN_APPLE, 100),
        Map.entry(Items.NETHER_STAR, 100),
        Map.entry(Items.ROTTEN_FLESH, -5),
        Map.entry(Items.SPIDER_EYE, -10),
        Map.entry(Items.FERMENTED_SPIDER_EYE, -20),
        Map.entry(Items.POISONOUS_POTATO, -10),
        Map.entry(Items.PUFFERFISH, -20)
    );

    public LuckyTuningRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        return calculate(input) != null;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        RecipeResult result = calculate(input);
        if (result == null) {
            return ItemStack.EMPTY;
        }
        ItemStack output = result.source().copyWithCount(1);
        LuckyBlockItem.setLuck(output, result.luck());
        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LuckyCobblemonMod.LUCK_TUNING_RECIPE;
    }

    private static RecipeResult calculate(CraftingRecipeInput input) {
        ItemStack luckyBlock = ItemStack.EMPTY;
        int change = 0;
        int modifierCount = 0;

        for (int slot = 0; slot < input.getSize(); slot++) {
            ItemStack stack = input.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof LuckyBlockItem) {
                if (!luckyBlock.isEmpty() || stack.getCount() != 1) {
                    return null;
                }
                luckyBlock = stack;
                continue;
            }
            Integer modifier = MODIFIERS.get(stack.getItem());
            if (modifier == null) {
                return null;
            }
            change += modifier;
            modifierCount++;
        }

        if (luckyBlock.isEmpty() || modifierCount == 0) {
            return null;
        }
        int oldLuck = LuckyBlockItem.getLuck(luckyBlock);
        int newLuck = Math.clamp(oldLuck + change, -100, 100);
        return newLuck == oldLuck ? null : new RecipeResult(luckyBlock, newLuck);
    }

    private record RecipeResult(ItemStack source, int luck) {
    }
}
