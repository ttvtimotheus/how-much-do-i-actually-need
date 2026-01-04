package dev.hmdain.client.logic;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientState {
    
    private static final ClientState INSTANCE = new ClientState();
    
    public static ClientState getInstance() {
        return INSTANCE;
    }
    
    private final List<ItemStack> targets = new ArrayList<>();
    private final Map<Item, Long> results = new HashMap<>();
    private boolean hudEnabled = true;
    
    private final RecipeResolver recipeResolver = new RecipeResolver();
    private final CostCalculator costCalculator = new CostCalculator(recipeResolver);

    public List<ItemStack> getTargets() {
        return targets;
    }

    public Map<Item, Long> getResults() {
        return results;
    }

    public boolean isHudEnabled() {
        return hudEnabled;
    }

    public void setHudEnabled(boolean hudEnabled) {
        this.hudEnabled = hudEnabled;
    }
    
    public void addTarget(Item item) {
        for (ItemStack stack : targets) {
            if (stack.getItem() == item) {
                stack.setCount(stack.getCount() + 1);
                recalculate();
                return;
            }
        }
        targets.add(new ItemStack(item));
        recalculate();
    }
    
    public void removeTarget(ItemStack stack) {
        targets.remove(stack);
        recalculate();
    }
    
    public void recalculate() {
        costCalculator.calculate(targets);
        results.clear();
        results.putAll(costCalculator.getResults());
    }
}
