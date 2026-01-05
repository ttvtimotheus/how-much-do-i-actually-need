package dev.hmdain.client.logic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * RecipeResolver with hardcoded vanilla recipes for reliable client-side recipe lookup.
 * This approach works without needing unlocked recipes in the recipe book.
 */
public class RecipeResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger("HowMuchDoIActuallyNeed");
    private final Map<Item, RecipeData> recipeDatabase = new HashMap<>();
    private final Map<Item, Optional<RecipeData>> recipeCache = new HashMap<>();
    private boolean initialized = false;

    public RecipeResolver() {
    }

    /**
     * Attempts to find recipe data for an item.
     */
    public Optional<RecipeData> findRecipe(Item item) {
        if (!initialized) {
            initializeRecipeDatabase();
            initialized = true;
        }
        
        if (recipeCache.containsKey(item)) {
            return recipeCache.get(item);
        }
        
        Optional<RecipeData> recipe = Optional.ofNullable(recipeDatabase.get(item));
        recipeCache.put(item, recipe);
        return recipe;
    }
    
    private void initializeRecipeDatabase() {
        LOGGER.info("Initializing recipe database...");
        
        // === WOOD PRODUCTS ===
        // Planks from logs (all wood types)
        addRecipe(Items.OAK_PLANKS, 4, Items.OAK_LOG, 1);
        addRecipe(Items.SPRUCE_PLANKS, 4, Items.SPRUCE_LOG, 1);
        addRecipe(Items.BIRCH_PLANKS, 4, Items.BIRCH_LOG, 1);
        addRecipe(Items.JUNGLE_PLANKS, 4, Items.JUNGLE_LOG, 1);
        addRecipe(Items.ACACIA_PLANKS, 4, Items.ACACIA_LOG, 1);
        addRecipe(Items.DARK_OAK_PLANKS, 4, Items.DARK_OAK_LOG, 1);
        addRecipe(Items.MANGROVE_PLANKS, 4, Items.MANGROVE_LOG, 1);
        addRecipe(Items.CHERRY_PLANKS, 4, Items.CHERRY_LOG, 1);
        addRecipe(Items.CRIMSON_PLANKS, 4, Items.CRIMSON_STEM, 1);
        addRecipe(Items.WARPED_PLANKS, 4, Items.WARPED_STEM, 1);
        addRecipe(Items.BAMBOO_PLANKS, 2, Items.BAMBOO_BLOCK, 1);
        
        // Sticks
        addRecipe(Items.STICK, 4, Items.OAK_PLANKS, 2);
        
        // Crafting Table
        addRecipe(Items.CRAFTING_TABLE, 1, Items.OAK_PLANKS, 4);
        
        // Chest
        addRecipe(Items.CHEST, 1, Items.OAK_PLANKS, 8);
        
        // Bookshelf (3 books + 6 planks)
        addRecipeMulti(Items.BOOKSHELF, 1, 
            new IngredientData(Items.BOOK, 3),
            new IngredientData(Items.OAK_PLANKS, 6));
        
        // Book (3 paper + 1 leather)
        addRecipeMulti(Items.BOOK, 1,
            new IngredientData(Items.PAPER, 3),
            new IngredientData(Items.LEATHER, 1));
        
        // Paper
        addRecipe(Items.PAPER, 3, Items.SUGAR_CANE, 3);
        
        // === TOOLS ===
        // Wooden tools
        addRecipeMulti(Items.WOODEN_PICKAXE, 1, new IngredientData(Items.OAK_PLANKS, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.WOODEN_AXE, 1, new IngredientData(Items.OAK_PLANKS, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.WOODEN_SHOVEL, 1, new IngredientData(Items.OAK_PLANKS, 1), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.WOODEN_SWORD, 1, new IngredientData(Items.OAK_PLANKS, 2), new IngredientData(Items.STICK, 1));
        addRecipeMulti(Items.WOODEN_HOE, 1, new IngredientData(Items.OAK_PLANKS, 2), new IngredientData(Items.STICK, 2));
        
        // Stone tools
        addRecipeMulti(Items.STONE_PICKAXE, 1, new IngredientData(Items.COBBLESTONE, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.STONE_AXE, 1, new IngredientData(Items.COBBLESTONE, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.STONE_SHOVEL, 1, new IngredientData(Items.COBBLESTONE, 1), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.STONE_SWORD, 1, new IngredientData(Items.COBBLESTONE, 2), new IngredientData(Items.STICK, 1));
        addRecipeMulti(Items.STONE_HOE, 1, new IngredientData(Items.COBBLESTONE, 2), new IngredientData(Items.STICK, 2));
        
        // Iron tools
        addRecipeMulti(Items.IRON_PICKAXE, 1, new IngredientData(Items.IRON_INGOT, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.IRON_AXE, 1, new IngredientData(Items.IRON_INGOT, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.IRON_SHOVEL, 1, new IngredientData(Items.IRON_INGOT, 1), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.IRON_SWORD, 1, new IngredientData(Items.IRON_INGOT, 2), new IngredientData(Items.STICK, 1));
        addRecipeMulti(Items.IRON_HOE, 1, new IngredientData(Items.IRON_INGOT, 2), new IngredientData(Items.STICK, 2));
        
        // Diamond tools
        addRecipeMulti(Items.DIAMOND_PICKAXE, 1, new IngredientData(Items.DIAMOND, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.DIAMOND_AXE, 1, new IngredientData(Items.DIAMOND, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.DIAMOND_SHOVEL, 1, new IngredientData(Items.DIAMOND, 1), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.DIAMOND_SWORD, 1, new IngredientData(Items.DIAMOND, 2), new IngredientData(Items.STICK, 1));
        addRecipeMulti(Items.DIAMOND_HOE, 1, new IngredientData(Items.DIAMOND, 2), new IngredientData(Items.STICK, 2));
        
        // Golden tools
        addRecipeMulti(Items.GOLDEN_PICKAXE, 1, new IngredientData(Items.GOLD_INGOT, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.GOLDEN_AXE, 1, new IngredientData(Items.GOLD_INGOT, 3), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.GOLDEN_SHOVEL, 1, new IngredientData(Items.GOLD_INGOT, 1), new IngredientData(Items.STICK, 2));
        addRecipeMulti(Items.GOLDEN_SWORD, 1, new IngredientData(Items.GOLD_INGOT, 2), new IngredientData(Items.STICK, 1));
        addRecipeMulti(Items.GOLDEN_HOE, 1, new IngredientData(Items.GOLD_INGOT, 2), new IngredientData(Items.STICK, 2));
        
        // === ARMOR ===
        // Iron armor
        addRecipe(Items.IRON_HELMET, 1, Items.IRON_INGOT, 5);
        addRecipe(Items.IRON_CHESTPLATE, 1, Items.IRON_INGOT, 8);
        addRecipe(Items.IRON_LEGGINGS, 1, Items.IRON_INGOT, 7);
        addRecipe(Items.IRON_BOOTS, 1, Items.IRON_INGOT, 4);
        
        // Diamond armor
        addRecipe(Items.DIAMOND_HELMET, 1, Items.DIAMOND, 5);
        addRecipe(Items.DIAMOND_CHESTPLATE, 1, Items.DIAMOND, 8);
        addRecipe(Items.DIAMOND_LEGGINGS, 1, Items.DIAMOND, 7);
        addRecipe(Items.DIAMOND_BOOTS, 1, Items.DIAMOND, 4);
        
        // Golden armor
        addRecipe(Items.GOLDEN_HELMET, 1, Items.GOLD_INGOT, 5);
        addRecipe(Items.GOLDEN_CHESTPLATE, 1, Items.GOLD_INGOT, 8);
        addRecipe(Items.GOLDEN_LEGGINGS, 1, Items.GOLD_INGOT, 7);
        addRecipe(Items.GOLDEN_BOOTS, 1, Items.GOLD_INGOT, 4);
        
        // Leather armor
        addRecipe(Items.LEATHER_HELMET, 1, Items.LEATHER, 5);
        addRecipe(Items.LEATHER_CHESTPLATE, 1, Items.LEATHER, 8);
        addRecipe(Items.LEATHER_LEGGINGS, 1, Items.LEATHER, 7);
        addRecipe(Items.LEATHER_BOOTS, 1, Items.LEATHER, 4);
        
        // === BUILDING BLOCKS ===
        // Stone variants
        addRecipe(Items.STONE_BRICKS, 4, Items.STONE, 4);
        addRecipe(Items.COBBLESTONE_WALL, 6, Items.COBBLESTONE, 6);
        addRecipe(Items.STONE_BRICK_WALL, 6, Items.STONE_BRICKS, 6);
        addRecipe(Items.COBBLESTONE_STAIRS, 4, Items.COBBLESTONE, 6);
        addRecipe(Items.STONE_BRICK_STAIRS, 4, Items.STONE_BRICKS, 6);
        addRecipe(Items.COBBLESTONE_SLAB, 6, Items.COBBLESTONE, 3);
        addRecipe(Items.STONE_BRICK_SLAB, 6, Items.STONE_BRICKS, 3);
        
        // Bricks
        addRecipe(Items.BRICKS, 1, Items.BRICK, 4);
        addRecipe(Items.BRICK_WALL, 6, Items.BRICKS, 6);
        addRecipe(Items.BRICK_STAIRS, 4, Items.BRICKS, 6);
        addRecipe(Items.BRICK_SLAB, 6, Items.BRICKS, 3);
        
        // Glass
        addRecipe(Items.GLASS_PANE, 16, Items.GLASS, 6);
        
        // Iron/Gold blocks
        addRecipe(Items.IRON_BLOCK, 1, Items.IRON_INGOT, 9);
        addRecipe(Items.GOLD_BLOCK, 1, Items.GOLD_INGOT, 9);
        addRecipe(Items.DIAMOND_BLOCK, 1, Items.DIAMOND, 9);
        addRecipe(Items.EMERALD_BLOCK, 1, Items.EMERALD, 9);
        addRecipe(Items.LAPIS_BLOCK, 1, Items.LAPIS_LAZULI, 9);
        addRecipe(Items.REDSTONE_BLOCK, 1, Items.REDSTONE, 9);
        addRecipe(Items.COAL_BLOCK, 1, Items.COAL, 9);
        addRecipe(Items.COPPER_BLOCK, 1, Items.COPPER_INGOT, 9);
        
        // Reverse: ingots from blocks
        addRecipe(Items.IRON_INGOT, 9, Items.IRON_BLOCK, 1);
        addRecipe(Items.GOLD_INGOT, 9, Items.GOLD_BLOCK, 1);
        addRecipe(Items.DIAMOND, 9, Items.DIAMOND_BLOCK, 1);
        
        // === REDSTONE ===
        addRecipeMulti(Items.PISTON, 1,
            new IngredientData(Items.OAK_PLANKS, 3),
            new IngredientData(Items.COBBLESTONE, 4),
            new IngredientData(Items.IRON_INGOT, 1),
            new IngredientData(Items.REDSTONE, 1));
        
        addRecipeMulti(Items.STICKY_PISTON, 1,
            new IngredientData(Items.PISTON, 1),
            new IngredientData(Items.SLIME_BALL, 1));
        
        addRecipe(Items.REDSTONE_TORCH, 1, Items.REDSTONE, 1, Items.STICK, 1);
        addRecipe(Items.LEVER, 1, Items.COBBLESTONE, 1, Items.STICK, 1);
        
        addRecipeMulti(Items.OBSERVER, 1,
            new IngredientData(Items.COBBLESTONE, 6),
            new IngredientData(Items.REDSTONE, 2),
            new IngredientData(Items.QUARTZ, 1));
        
        addRecipeMulti(Items.HOPPER, 1,
            new IngredientData(Items.IRON_INGOT, 5),
            new IngredientData(Items.CHEST, 1));
        
        addRecipeMulti(Items.DROPPER, 1,
            new IngredientData(Items.COBBLESTONE, 7),
            new IngredientData(Items.REDSTONE, 1));
        
        addRecipeMulti(Items.DISPENSER, 1,
            new IngredientData(Items.COBBLESTONE, 7),
            new IngredientData(Items.BOW, 1),
            new IngredientData(Items.REDSTONE, 1));
        
        addRecipeMulti(Items.REPEATER, 1,
            new IngredientData(Items.REDSTONE_TORCH, 2),
            new IngredientData(Items.REDSTONE, 1),
            new IngredientData(Items.STONE, 3));
        
        addRecipeMulti(Items.COMPARATOR, 1,
            new IngredientData(Items.REDSTONE_TORCH, 3),
            new IngredientData(Items.QUARTZ, 1),
            new IngredientData(Items.STONE, 3));
        
        // === MISC ===
        addRecipe(Items.TORCH, 4, Items.COAL, 1, Items.STICK, 1);
        addRecipe(Items.LADDER, 3, Items.STICK, 7);
        addRecipeMulti(Items.BOW, 1, new IngredientData(Items.STICK, 3), new IngredientData(Items.STRING, 3));
        addRecipeMulti(Items.ARROW, 4, new IngredientData(Items.FLINT, 1), new IngredientData(Items.STICK, 1), new IngredientData(Items.FEATHER, 1));
        addRecipe(Items.FURNACE, 1, Items.COBBLESTONE, 8);
        addRecipe(Items.SMOKER, 1, Items.FURNACE, 1, Items.OAK_LOG, 4);
        addRecipe(Items.BLAST_FURNACE, 1, Items.FURNACE, 1, Items.IRON_INGOT, 5, Items.SMOOTH_STONE, 3);
        addRecipeMulti(Items.ANVIL, 1, new IngredientData(Items.IRON_BLOCK, 3), new IngredientData(Items.IRON_INGOT, 4));
        addRecipeMulti(Items.ENCHANTING_TABLE, 1, new IngredientData(Items.BOOK, 1), new IngredientData(Items.DIAMOND, 2), new IngredientData(Items.OBSIDIAN, 4));
        addRecipe(Items.ENDER_CHEST, 1, Items.OBSIDIAN, 8, Items.ENDER_EYE, 1);
        addRecipe(Items.ENDER_EYE, 1, Items.ENDER_PEARL, 1, Items.BLAZE_POWDER, 1);
        addRecipe(Items.BLAZE_POWDER, 2, Items.BLAZE_ROD, 1);
        addRecipeMulti(Items.BEACON, 1, new IngredientData(Items.GLASS, 5), new IngredientData(Items.NETHER_STAR, 1), new IngredientData(Items.OBSIDIAN, 3));
        
        // Rails
        addRecipe(Items.RAIL, 16, Items.IRON_INGOT, 6, Items.STICK, 1);
        addRecipeMulti(Items.POWERED_RAIL, 6, new IngredientData(Items.GOLD_INGOT, 6), new IngredientData(Items.STICK, 1), new IngredientData(Items.REDSTONE, 1));
        addRecipeMulti(Items.DETECTOR_RAIL, 6, new IngredientData(Items.IRON_INGOT, 6), new IngredientData(Items.STONE_PRESSURE_PLATE, 1), new IngredientData(Items.REDSTONE, 1));
        addRecipeMulti(Items.ACTIVATOR_RAIL, 6, new IngredientData(Items.IRON_INGOT, 6), new IngredientData(Items.STICK, 2), new IngredientData(Items.REDSTONE_TORCH, 1));
        addRecipe(Items.MINECART, 1, Items.IRON_INGOT, 5);
        addRecipe(Items.CHEST_MINECART, 1, Items.MINECART, 1, Items.CHEST, 1);
        addRecipe(Items.HOPPER_MINECART, 1, Items.MINECART, 1, Items.HOPPER, 1);
        
        // Beds
        addRecipe(Items.WHITE_BED, 1, Items.WHITE_WOOL, 3, Items.OAK_PLANKS, 3);
        
        // Doors and trapdoors
        addRecipe(Items.OAK_DOOR, 3, Items.OAK_PLANKS, 6);
        addRecipe(Items.IRON_DOOR, 3, Items.IRON_INGOT, 6);
        addRecipe(Items.OAK_TRAPDOOR, 2, Items.OAK_PLANKS, 6);
        addRecipe(Items.IRON_TRAPDOOR, 1, Items.IRON_INGOT, 4);
        
        // Fences and gates
        addRecipe(Items.OAK_FENCE, 3, Items.OAK_PLANKS, 4, Items.STICK, 2);
        addRecipe(Items.OAK_FENCE_GATE, 1, Items.STICK, 4, Items.OAK_PLANKS, 2);
        
        // Signs
        addRecipe(Items.OAK_SIGN, 3, Items.OAK_PLANKS, 6, Items.STICK, 1);
        
        // Buttons and pressure plates
        addRecipe(Items.OAK_BUTTON, 1, Items.OAK_PLANKS, 1);
        addRecipe(Items.STONE_BUTTON, 1, Items.STONE, 1);
        addRecipe(Items.OAK_PRESSURE_PLATE, 1, Items.OAK_PLANKS, 2);
        addRecipe(Items.STONE_PRESSURE_PLATE, 1, Items.STONE, 2);
        addRecipe(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 1, Items.IRON_INGOT, 2);
        addRecipe(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, Items.GOLD_INGOT, 2);
        
        // Bucket and boat
        addRecipe(Items.BUCKET, 1, Items.IRON_INGOT, 3);
        addRecipe(Items.OAK_BOAT, 1, Items.OAK_PLANKS, 5);
        
        // Shears and flint&steel
        addRecipe(Items.SHEARS, 1, Items.IRON_INGOT, 2);
        addRecipe(Items.FLINT_AND_STEEL, 1, Items.IRON_INGOT, 1, Items.FLINT, 1);
        
        // Compass and clock
        addRecipe(Items.COMPASS, 1, Items.IRON_INGOT, 4, Items.REDSTONE, 1);
        addRecipe(Items.CLOCK, 1, Items.GOLD_INGOT, 4, Items.REDSTONE, 1);
        
        // Map and Item Frame
        addRecipe(Items.MAP, 1, Items.PAPER, 8, Items.COMPASS, 1);
        addRecipe(Items.ITEM_FRAME, 1, Items.STICK, 8, Items.LEATHER, 1);
        
        // Brewing
        addRecipe(Items.BREWING_STAND, 1, Items.BLAZE_ROD, 1, Items.COBBLESTONE, 3);
        addRecipe(Items.CAULDRON, 1, Items.IRON_INGOT, 7);
        addRecipe(Items.GLASS_BOTTLE, 3, Items.GLASS, 3);
        
        // Wool to carpet
        addRecipe(Items.WHITE_CARPET, 3, Items.WHITE_WOOL, 2);
        
        LOGGER.info("Recipe database initialized with {} recipes", recipeDatabase.size());
    }
    
    private void addRecipe(Item result, int resultCount, Item ingredient, int ingredientCount) {
        recipeDatabase.put(result, new RecipeData(result, resultCount, 
            List.of(new IngredientData(ingredient, ingredientCount))));
    }
    
    private void addRecipe(Item result, int resultCount, Item ing1, int count1, Item ing2, int count2) {
        recipeDatabase.put(result, new RecipeData(result, resultCount,
            List.of(new IngredientData(ing1, count1), new IngredientData(ing2, count2))));
    }
    
    private void addRecipe(Item result, int resultCount, Item ing1, int count1, Item ing2, int count2, Item ing3, int count3) {
        recipeDatabase.put(result, new RecipeData(result, resultCount,
            List.of(new IngredientData(ing1, count1), new IngredientData(ing2, count2), new IngredientData(ing3, count3))));
    }
    
    private void addRecipeMulti(Item result, int resultCount, IngredientData... ingredients) {
        recipeDatabase.put(result, new RecipeData(result, resultCount, List.of(ingredients)));
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
