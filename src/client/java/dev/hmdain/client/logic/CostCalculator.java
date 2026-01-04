package dev.hmdain.client.logic;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates the total material costs for crafting target items.
 * Works with RecipeResolver to break down recipes into base materials.
 */
public class CostCalculator {

    private final RecipeResolver recipeResolver;
    private final Map<Item, Long> materialCounts = new HashMap<>();
    private final Map<Item, Integer> recursionDepth = new HashMap<>();
    private static final int MAX_DEPTH = 10;

    public CostCalculator(RecipeResolver recipeResolver) {
        this.recipeResolver = recipeResolver;
    }

    public void calculate(List<ItemStack> targets) {
        materialCounts.clear();
        recursionDepth.clear();
        for (ItemStack target : targets) {
            processItem(target.getItem(), target.getCount());
        }
    }

    private void processItem(Item item, long count) {
        int depth = recursionDepth.getOrDefault(item, 0);
        if (depth > MAX_DEPTH) {
            addMaterial(item, count);
            return;
        }

        var recipeOptional = recipeResolver.findRecipe(item);

        if (recipeOptional.isEmpty()) {
            // No recipe found - this is a base material
            addMaterial(item, count);
            return;
        }

        RecipeResolver.RecipeData recipeData = recipeOptional.get();
        int outputCount = recipeData.resultCount();
        if (outputCount <= 0) outputCount = 1;

        long craftsNeeded = (long) Math.ceil((double) count / outputCount);

        recursionDepth.put(item, depth + 1);

        for (RecipeResolver.IngredientData ingredient : recipeData.ingredients()) {
            processItem(ingredient.item(), craftsNeeded * ingredient.count());
        }

        recursionDepth.put(item, depth);
    }

    private void addMaterial(Item item, long count) {
        materialCounts.merge(item, count, Long::sum);
    }

    public Map<Item, Long> getResults() {
        return materialCounts;
    }
}
