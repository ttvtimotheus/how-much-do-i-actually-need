package dev.hmdain.client.logic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * RecipeResolver for client-side recipe lookup using ClientRecipeBook + RecipeDisplay API.
 * This works in Minecraft 1.21.5 by accessing the synced recipe data from the recipe book.
 */
public class RecipeResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger("HowMuchDoIActuallyNeed");
    private final Map<Item, Optional<RecipeData>> recipeCache = new HashMap<>();
    private final Minecraft client = Minecraft.getInstance();
    private boolean debugLogged = false;

    public RecipeResolver() {
    }

    /**
     * Attempts to find recipe data for an item using the ClientRecipeBook.
     */
    public Optional<RecipeData> findRecipe(Item item) {
        if (recipeCache.containsKey(item)) {
            return recipeCache.get(item);
        }
        
        Optional<RecipeData> recipe = findRecipeInternal(item);
        recipeCache.put(item, recipe);
        return recipe;
    }
    
    private Optional<RecipeData> findRecipeInternal(Item targetItem) {
        if (client.player == null || client.level == null) {
            return Optional.empty();
        }
        
        ClientRecipeBook recipeBook = client.player.getRecipeBook();
        ContextMap context = SlotDisplayContext.fromLevel(client.level);
        
        // Debug: log recipe book status once
        List<RecipeCollection> collections = recipeBook.getCollections();
        if (!debugLogged) {
            LOGGER.info("RecipeBook has {} collections", collections.size());
            int totalRecipes = 0;
            for (RecipeCollection col : collections) {
                totalRecipes += col.getRecipes().size();
            }
            LOGGER.info("Total recipes in collections: {}", totalRecipes);
            debugLogged = true;
        }
        
        // Iterate through all recipe collections
        for (RecipeCollection collection : collections) {
            for (RecipeDisplayEntry entry : collection.getRecipes()) {
                RecipeDisplay display = entry.display();
                
                // Get result item from the display
                ItemStack resultStack = display.result().resolveForFirstStack(context);
                if (resultStack.isEmpty() || resultStack.getItem() != targetItem) {
                    continue;
                }
                
                // Found a matching recipe - extract ingredients
                List<IngredientData> ingredients = extractIngredients(display, context);
                if (!ingredients.isEmpty()) {
                    LOGGER.info("Found recipe for {}: {} ingredients", targetItem, ingredients.size());
                    return Optional.of(new RecipeData(
                        resultStack.getItem(),
                        resultStack.getCount(),
                        ingredients
                    ));
                }
            }
        }
        
        LOGGER.debug("No recipe found for {}", targetItem);
        return Optional.empty();
    }
    
    private List<IngredientData> extractIngredients(RecipeDisplay display, ContextMap context) {
        List<IngredientData> ingredients = new ArrayList<>();
        
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            // Shaped crafting recipe
            Map<Item, Integer> ingredientCounts = new HashMap<>();
            for (SlotDisplay slot : shaped.ingredients()) {
                ItemStack stack = slot.resolveForFirstStack(context);
                if (!stack.isEmpty()) {
                    ingredientCounts.merge(stack.getItem(), 1, Integer::sum);
                }
            }
            for (Map.Entry<Item, Integer> e : ingredientCounts.entrySet()) {
                ingredients.add(new IngredientData(e.getKey(), e.getValue()));
            }
        } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            // Shapeless crafting recipe
            Map<Item, Integer> ingredientCounts = new HashMap<>();
            for (SlotDisplay slot : shapeless.ingredients()) {
                ItemStack stack = slot.resolveForFirstStack(context);
                if (!stack.isEmpty()) {
                    ingredientCounts.merge(stack.getItem(), 1, Integer::sum);
                }
            }
            for (Map.Entry<Item, Integer> e : ingredientCounts.entrySet()) {
                ingredients.add(new IngredientData(e.getKey(), e.getValue()));
            }
        } else if (display instanceof FurnaceRecipeDisplay furnace) {
            // Smelting recipe
            ItemStack inputStack = furnace.ingredient().resolveForFirstStack(context);
            if (!inputStack.isEmpty()) {
                ingredients.add(new IngredientData(inputStack.getItem(), 1));
            }
        } else if (display instanceof StonecutterRecipeDisplay stonecutter) {
            // Stonecutter recipe
            ItemStack inputStack = stonecutter.input().resolveForFirstStack(context);
            if (!inputStack.isEmpty()) {
                ingredients.add(new IngredientData(inputStack.getItem(), 1));
            }
        } else if (display instanceof SmithingRecipeDisplay smithing) {
            // Smithing recipe (template, base, addition)
            ItemStack templateStack = smithing.template().resolveForFirstStack(context);
            ItemStack baseStack = smithing.base().resolveForFirstStack(context);
            ItemStack additionStack = smithing.addition().resolveForFirstStack(context);
            
            if (!templateStack.isEmpty()) {
                ingredients.add(new IngredientData(templateStack.getItem(), 1));
            }
            if (!baseStack.isEmpty()) {
                ingredients.add(new IngredientData(baseStack.getItem(), 1));
            }
            if (!additionStack.isEmpty()) {
                ingredients.add(new IngredientData(additionStack.getItem(), 1));
            }
        }
        
        return ingredients;
    }
    
    public void clearCache() {
        recipeCache.clear();
    }
    
    /**
     * Record to hold recipe data.
     */
    public record RecipeData(Item resultItem, int resultCount, List<IngredientData> ingredients) {}
    
    /**
     * Record to hold ingredient data.
     */
    public record IngredientData(Item item, int count) {}
}
