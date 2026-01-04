package dev.hmdain.client.ui;

import dev.hmdain.client.logic.ClientState;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HowMuchScreen extends BaseOwoScreen<FlowLayout> {

    private final ClientState state = ClientState.getInstance();
    private FlowLayout itemGridContainer;
    private FlowLayout targetListContainer;
    private FlowLayout resultListContainer;
    private TextBoxComponent searchField;
    
    private static final int ITEM_SIZE = 18;
    private static final int MAX_ITEMS = 200;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }
    
    private int getGridColumns() {
        // Dynamically calculate columns based on available width
        var mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int availableWidth = (int)(screenWidth * 0.35); // 35% of screen for item grid
        return Math.max(4, Math.min(12, availableWidth / (ITEM_SIZE + 4)));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5));

        // Main container - responsive sizing
        var mainPanel = Containers.verticalFlow(Sizing.fill(95), Sizing.fill(90));
        mainPanel.surface(Surface.DARK_PANEL);
        mainPanel.padding(Insets.of(8));
        mainPanel.gap(6);

        // === HEADER ===
        var header = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        header.horizontalAlignment(HorizontalAlignment.CENTER);
        header.child(
            Components.label(Component.literal("How Much Do I Actually Need?"))
                .color(Color.ofRgb(0x55FFFF))
                .shadow(true)
        );
        mainPanel.child(header);

        // === MAIN CONTENT - Two columns ===
        var contentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));
        contentRow.gap(10);

        // --- LEFT PANEL: Item Selection ---
        var leftPanel = Containers.verticalFlow(Sizing.fill(45), Sizing.fill(100));
        leftPanel.gap(5);

        // Search section
        var searchLabel = Components.label(Component.literal("Search Items"))
                .color(Color.ofRgb(0xAAAAAA));
        leftPanel.child(searchLabel);

        searchField = Components.textBox(Sizing.fill(100));
        searchField.text("");
        searchField.onChanged().subscribe(this::onSearchChanged);
        leftPanel.child(searchField);

        // Item grid in scroll container
        var itemGridScroll = Containers.verticalScroll(
            Sizing.fill(100), 
            Sizing.fill(100),
            Containers.verticalFlow(Sizing.fill(100), Sizing.content())
        );
        itemGridScroll.surface(Surface.PANEL_INSET);
        itemGridScroll.padding(Insets.of(4));
        
        itemGridContainer = (FlowLayout) itemGridScroll.child();
        itemGridContainer.gap(2);
        
        populateItemGrid("");
        leftPanel.child(itemGridScroll);

        // --- RIGHT PANEL: Targets & Results ---
        var rightPanel = Containers.verticalFlow(Sizing.fill(55), Sizing.fill(100));
        rightPanel.gap(5);

        // Targets Section
        var targetHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        targetHeader.child(
            Components.label(Component.literal("Craft Targets"))
                .color(Color.ofRgb(0x55FF55))
        );
        var clearBtn = Components.button(Component.literal("Clear All"), btn -> {
            state.getTargets().clear();
            state.recalculate();
            updateUI();
        });
        clearBtn.sizing(Sizing.content(), Sizing.fixed(14));
        clearBtn.margins(Insets.left(5));
        targetHeader.child(clearBtn);
        rightPanel.child(targetHeader);

        var targetScroll = Containers.verticalScroll(
            Sizing.fill(100), 
            Sizing.fixed(80),
            Containers.verticalFlow(Sizing.fill(100), Sizing.content())
        );
        targetScroll.surface(Surface.PANEL_INSET);
        targetScroll.padding(Insets.of(4));
        targetListContainer = (FlowLayout) targetScroll.child();
        targetListContainer.gap(2);
        rightPanel.child(targetScroll);

        // Results Section
        rightPanel.child(
            Components.label(Component.literal("Required Base Materials"))
                .color(Color.ofRgb(0xFFAA00))
                .margins(Insets.top(5))
        );

        var resultScroll = Containers.verticalScroll(
            Sizing.fill(100), 
            Sizing.fill(100),
            Containers.verticalFlow(Sizing.fill(100), Sizing.content())
        );
        resultScroll.surface(Surface.PANEL_INSET);
        resultScroll.padding(Insets.of(4));
        resultListContainer = (FlowLayout) resultScroll.child();
        resultListContainer.gap(2);
        rightPanel.child(resultScroll);

        contentRow.child(leftPanel);
        contentRow.child(rightPanel);
        mainPanel.child(contentRow);

        rootComponent.child(mainPanel);
        
        // Initial UI update
        updateUI();
    }
    
    private void onSearchChanged(String search) {
        populateItemGrid(search);
    }
    
    private void populateItemGrid(String search) {
        itemGridContainer.clearChildren();
        
        String lowerSearch = search.toLowerCase().trim();
        List<Item> matchingItems = new ArrayList<>();
        
        // Filter items
        BuiltInRegistries.ITEM.stream()
            .filter(item -> item != Items.AIR)
            .filter(item -> {
                if (lowerSearch.isEmpty()) return true;
                String name = item.getDefaultInstance().getHoverName().getString().toLowerCase();
                return name.contains(lowerSearch);
            })
            .limit(MAX_ITEMS)
            .forEach(matchingItems::add);
        
        // Create grid rows
        FlowLayout currentRow = null;
        int itemsInRow = 0;
        
        for (Item item : matchingItems) {
            if (currentRow == null || itemsInRow >= getGridColumns()) {
                currentRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
                currentRow.gap(2);
                itemGridContainer.child(currentRow);
                itemsInRow = 0;
            }
            
            var itemButton = createItemButton(item);
            currentRow.child(itemButton);
            itemsInRow++;
        }
        
        // Show message if no items found
        if (matchingItems.isEmpty()) {
            itemGridContainer.child(
                Components.label(Component.literal("No items found"))
                    .color(Color.ofRgb(0x888888))
                    .margins(Insets.of(10))
            );
        }
    }
    
    private FlowLayout createItemButton(Item item) {
        var container = Containers.verticalFlow(Sizing.fixed(ITEM_SIZE + 2), Sizing.fixed(ITEM_SIZE + 2));
        container.horizontalAlignment(HorizontalAlignment.CENTER);
        container.verticalAlignment(VerticalAlignment.CENTER);
        container.surface(Surface.PANEL);
        container.cursorStyle(CursorStyle.HAND);
        
        var itemComponent = Components.item(item.getDefaultInstance());
        itemComponent.showOverlay(true);
        container.child(itemComponent);
        
        // Make clickable
        container.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button == 0) { // Left click
                addTarget(item, 1);
                return true;
            } else if (button == 1) { // Right click - add stack
                addTarget(item, 64);
                return true;
            }
            return false;
        });
        
        // Tooltip
        container.tooltip(item.getDefaultInstance().getHoverName());
        
        return container;
    }

    private void addTarget(Item item, int amount) {
        for (int i = 0; i < amount; i++) {
            state.addTarget(item);
        }
        updateUI();
    }

    private void removeTarget(ItemStack stack) {
        state.removeTarget(stack);
        updateUI();
    }
    
    private void adjustTargetCount(ItemStack stack, int delta) {
        int newCount = stack.getCount() + delta;
        if (newCount <= 0) {
            state.removeTarget(stack);
        } else {
            stack.setCount(newCount);
            state.recalculate();
        }
        updateUI();
    }

    private void updateUI() {
        updateTargetList();
        updateResultList();
    }
    
    private void updateTargetList() {
        targetListContainer.clearChildren();
        
        if (state.getTargets().isEmpty()) {
            targetListContainer.child(
                Components.label(Component.literal("Click items to add targets"))
                    .color(Color.ofRgb(0x666666))
                    .margins(Insets.of(5))
            );
            return;
        }
        
        for (ItemStack stack : state.getTargets()) {
            var row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
            row.verticalAlignment(VerticalAlignment.CENTER);
            row.gap(4);
            row.padding(Insets.horizontal(2));
            
            // Item icon
            var itemComp = Components.item(stack);
            row.child(itemComp);
            
            // Item name (truncated)
            String name = stack.getHoverName().getString();
            if (name.length() > 15) name = name.substring(0, 14) + "...";
            row.child(
                Components.label(Component.literal(name))
                    .color(Color.WHITE)
                    .sizing(Sizing.fill(100), Sizing.content())
            );
            
            // Quantity controls
            var minusBtn = Components.button(Component.literal("-"), btn -> adjustTargetCount(stack, -1));
            minusBtn.sizing(Sizing.fixed(14), Sizing.fixed(14));
            row.child(minusBtn);
            
            row.child(
                Components.label(Component.literal(String.valueOf(stack.getCount())))
                    .color(Color.ofRgb(0xFFFF55))
                    .horizontalTextAlignment(HorizontalAlignment.CENTER)
                    .sizing(Sizing.fixed(24), Sizing.content())
            );
            
            var plusBtn = Components.button(Component.literal("+"), btn -> adjustTargetCount(stack, 1));
            plusBtn.sizing(Sizing.fixed(14), Sizing.fixed(14));
            row.child(plusBtn);
            
            // Remove button
            var removeBtn = Components.button(Component.literal("X"), btn -> removeTarget(stack));
            removeBtn.sizing(Sizing.fixed(14), Sizing.fixed(14));
            row.child(removeBtn);
            
            targetListContainer.child(row);
        }
    }
    
    private void updateResultList() {
        resultListContainer.clearChildren();
        
        var results = state.getResults();
        
        if (results.isEmpty()) {
            resultListContainer.child(
                Components.label(Component.literal("Add targets to see materials"))
                    .color(Color.ofRgb(0x666666))
                    .margins(Insets.of(5))
            );
            return;
        }
        
        // Sort by count descending
        results.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .forEach(entry -> {
                var item = entry.getKey();
                var count = entry.getValue();
                
                var row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(18));
                row.verticalAlignment(VerticalAlignment.CENTER);
                row.gap(4);
                row.padding(Insets.horizontal(2));
                
                // Item icon
                row.child(Components.item(item.getDefaultInstance()));
                
                // Count with color based on amount
                int countColor;
                if (count > 1000) countColor = 0xFF5555; // Red for large amounts
                else if (count > 100) countColor = 0xFFAA00; // Orange for medium
                else countColor = 0x55FF55; // Green for small
                
                row.child(
                    Components.label(Component.literal(formatCount(count) + "x"))
                        .color(Color.ofRgb(countColor))
                        .sizing(Sizing.fixed(45), Sizing.content())
                );
                
                // Item name
                String name = item.getDefaultInstance().getHoverName().getString();
                if (name.length() > 18) name = name.substring(0, 17) + "...";
                row.child(
                    Components.label(Component.literal(name))
                        .color(Color.WHITE)
                );
                
                resultListContainer.child(row);
            });
        
        // Total items summary
        long totalItems = results.values().stream().mapToLong(Long::longValue).sum();
        int uniqueItems = results.size();
        
        resultListContainer.child(
            Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1))
                .surface(Surface.flat(0x44FFFFFF))
                .margins(Insets.vertical(4))
        );
        
        resultListContainer.child(
            Components.label(Component.literal("Total: " + formatCount(totalItems) + " items (" + uniqueItems + " types)"))
                .color(Color.ofRgb(0xAAAAAA))
                .margins(Insets.top(2))
        );
    }
    
    private String formatCount(long count) {
        if (count >= 1_000_000) {
            return String.format("%.1fM", count / 1_000_000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        }
        return String.valueOf(count);
    }
}
