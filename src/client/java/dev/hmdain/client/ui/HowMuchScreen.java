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
    
    private static final int ITEM_SIZE = 16;
    private static final int MAX_ITEMS = 150;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }
    
    private int getGridColumns() {
        var mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        // Calculate based on left panel width (40% of 80% of screen)
        int panelWidth = (int)(screenWidth * 0.32) - 16;
        return Math.max(3, Math.min(9, panelWidth / (ITEM_SIZE + 3)));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var mc = Minecraft.getInstance();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER);

        // Main window - sized relative to screen
        int windowWidth = Math.min(320, (int)(mc.getWindow().getGuiScaledWidth() * 0.85));
        int windowHeight = Math.min(220, (int)(screenHeight * 0.80));
        
        var mainPanel = Containers.verticalFlow(Sizing.fixed(windowWidth), Sizing.fixed(windowHeight));
        mainPanel.surface(Surface.PANEL);
        mainPanel.padding(Insets.of(6));
        mainPanel.gap(4);

        // Header
        var header = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
        header.horizontalAlignment(HorizontalAlignment.CENTER);
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.child(
            Components.label(Component.literal("How Much Do I Actually Need?"))
                .color(Color.ofRgb(0x55FFFF))
                .shadow(true)
        );
        mainPanel.child(header);

        // Main content area
        var contentRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100));
        contentRow.gap(6);

        // === LEFT: Item Browser ===
        var leftPanel = Containers.verticalFlow(Sizing.fill(40), Sizing.fill(100));
        leftPanel.gap(3);
        leftPanel.surface(Surface.DARK_PANEL);
        leftPanel.padding(Insets.of(4));

        // Search box
        searchField = Components.textBox(Sizing.fill(100));
        searchField.text("");
        searchField.onChanged().subscribe(this::onSearchChanged);
        leftPanel.child(searchField);

        // Item grid
        var itemGridScroll = Containers.verticalScroll(
            Sizing.fill(100), 
            Sizing.fill(100),
            Containers.verticalFlow(Sizing.content(), Sizing.content())
        );
        itemGridScroll.surface(Surface.PANEL_INSET);
        itemGridScroll.padding(Insets.of(2));
        
        itemGridContainer = (FlowLayout) itemGridScroll.child();
        itemGridContainer.gap(1);
        itemGridContainer.horizontalAlignment(HorizontalAlignment.LEFT);
        
        populateItemGrid("");
        leftPanel.child(itemGridScroll);

        // === RIGHT: Targets & Results ===
        var rightPanel = Containers.verticalFlow(Sizing.fill(60), Sizing.fill(100));
        rightPanel.gap(3);

        // Targets section
        var targetSection = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(35));
        targetSection.surface(Surface.DARK_PANEL);
        targetSection.padding(Insets.of(4));
        targetSection.gap(2);
        
        var targetHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
        targetHeader.verticalAlignment(VerticalAlignment.CENTER);
        targetHeader.child(
            Components.label(Component.literal("Targets"))
                .color(Color.ofRgb(0x55FF55))
        );
        
        var clearBtn = Components.button(Component.literal("Clear"), btn -> {
            state.getTargets().clear();
            state.recalculate();
            updateUI();
        });
        clearBtn.sizing(Sizing.fixed(32), Sizing.fixed(12));
        clearBtn.margins(Insets.left(4));
        targetHeader.child(clearBtn);
        targetSection.child(targetHeader);

        var targetScroll = Containers.verticalScroll(
            Sizing.fill(100), 
            Sizing.fill(100),
            Containers.verticalFlow(Sizing.fill(100), Sizing.content())
        );
        targetScroll.surface(Surface.PANEL_INSET);
        targetScroll.padding(Insets.of(2));
        targetListContainer = (FlowLayout) targetScroll.child();
        targetListContainer.gap(1);
        targetSection.child(targetScroll);
        rightPanel.child(targetSection);

        // Results section
        var resultSection = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(65));
        resultSection.surface(Surface.DARK_PANEL);
        resultSection.padding(Insets.of(4));
        resultSection.gap(2);
        
        resultSection.child(
            Components.label(Component.literal("Base Materials"))
                .color(Color.ofRgb(0xFFAA00))
        );

        var resultScroll = Containers.verticalScroll(
            Sizing.fill(100), 
            Sizing.fill(100),
            Containers.verticalFlow(Sizing.fill(100), Sizing.content())
        );
        resultScroll.surface(Surface.PANEL_INSET);
        resultScroll.padding(Insets.of(2));
        resultListContainer = (FlowLayout) resultScroll.child();
        resultListContainer.gap(1);
        resultSection.child(resultScroll);
        rightPanel.child(resultSection);

        contentRow.child(leftPanel);
        contentRow.child(rightPanel);
        mainPanel.child(contentRow);

        rootComponent.child(mainPanel);
        updateUI();
    }
    
    private void onSearchChanged(String search) {
        populateItemGrid(search);
    }
    
    private void populateItemGrid(String search) {
        itemGridContainer.clearChildren();
        
        String lowerSearch = search.toLowerCase().trim();
        List<Item> matchingItems = new ArrayList<>();
        
        BuiltInRegistries.ITEM.stream()
            .filter(item -> item != Items.AIR)
            .filter(item -> {
                if (lowerSearch.isEmpty()) return true;
                String name = item.getDefaultInstance().getHoverName().getString().toLowerCase();
                return name.contains(lowerSearch);
            })
            .limit(MAX_ITEMS)
            .forEach(matchingItems::add);
        
        int cols = getGridColumns();
        FlowLayout currentRow = null;
        int itemsInRow = 0;
        
        for (Item item : matchingItems) {
            if (currentRow == null || itemsInRow >= cols) {
                currentRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
                currentRow.gap(1);
                itemGridContainer.child(currentRow);
                itemsInRow = 0;
            }
            
            var itemBtn = createItemButton(item);
            currentRow.child(itemBtn);
            itemsInRow++;
        }
        
        if (matchingItems.isEmpty()) {
            itemGridContainer.child(
                Components.label(Component.literal("No items"))
                    .color(Color.ofRgb(0x666666))
                    .margins(Insets.of(4))
            );
        }
    }
    
    private FlowLayout createItemButton(Item item) {
        var btn = Containers.verticalFlow(Sizing.fixed(ITEM_SIZE + 2), Sizing.fixed(ITEM_SIZE + 2));
        btn.horizontalAlignment(HorizontalAlignment.CENTER);
        btn.verticalAlignment(VerticalAlignment.CENTER);
        btn.surface(Surface.PANEL);
        btn.cursorStyle(CursorStyle.HAND);
        
        var itemComp = Components.item(item.getDefaultInstance());
        btn.child(itemComp);
        
        btn.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button == 0) {
                addTarget(item, 1);
                return true;
            } else if (button == 1) {
                addTarget(item, 64);
                return true;
            }
            return false;
        });
        
        btn.tooltip(item.getDefaultInstance().getHoverName());
        return btn;
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
                Components.label(Component.literal("Click items to add"))
                    .color(Color.ofRgb(0x666666))
            );
            return;
        }
        
        for (ItemStack stack : state.getTargets()) {
            var row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(16));
            row.verticalAlignment(VerticalAlignment.CENTER);
            row.gap(2);
            
            row.child(Components.item(stack));
            
            String name = stack.getHoverName().getString();
            if (name.length() > 12) name = name.substring(0, 11) + "..";
            row.child(
                Components.label(Component.literal(name))
                    .color(Color.WHITE)
                    .sizing(Sizing.fill(100), Sizing.content())
            );
            
            var minusBtn = Components.button(Component.literal("-"), btn -> adjustTargetCount(stack, -1));
            minusBtn.sizing(Sizing.fixed(12), Sizing.fixed(12));
            row.child(minusBtn);
            
            var countLabel = Components.label(Component.literal(String.valueOf(stack.getCount())));
            countLabel.color(Color.ofRgb(0xFFFF55));
            countLabel.sizing(Sizing.fixed(20), Sizing.content());
            row.child(countLabel);
            
            var plusBtn = Components.button(Component.literal("+"), btn -> adjustTargetCount(stack, 1));
            plusBtn.sizing(Sizing.fixed(12), Sizing.fixed(12));
            row.child(plusBtn);
            
            var xBtn = Components.button(Component.literal("x"), btn -> removeTarget(stack));
            xBtn.sizing(Sizing.fixed(12), Sizing.fixed(12));
            row.child(xBtn);
            
            targetListContainer.child(row);
        }
    }
    
    private void updateResultList() {
        resultListContainer.clearChildren();
        
        var results = state.getResults();
        
        if (results.isEmpty()) {
            resultListContainer.child(
                Components.label(Component.literal("Add targets first"))
                    .color(Color.ofRgb(0x666666))
            );
            return;
        }
        
        results.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .forEach(entry -> {
                var item = entry.getKey();
                var count = entry.getValue();
                
                var row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(14));
                row.verticalAlignment(VerticalAlignment.CENTER);
                row.gap(3);
                
                row.child(Components.item(item.getDefaultInstance()));
                
                int color = count > 1000 ? 0xFF5555 : (count > 100 ? 0xFFAA00 : 0x55FF55);
                row.child(
                    Components.label(Component.literal(formatCount(count) + "x"))
                        .color(Color.ofRgb(color))
                        .sizing(Sizing.fixed(36), Sizing.content())
                );
                
                String name = item.getDefaultInstance().getHoverName().getString();
                if (name.length() > 14) name = name.substring(0, 13) + "..";
                row.child(
                    Components.label(Component.literal(name))
                        .color(Color.WHITE)
                );
                
                resultListContainer.child(row);
            });
        
        long total = results.values().stream().mapToLong(Long::longValue).sum();
        resultListContainer.child(
            Components.label(Component.literal("Total: " + formatCount(total)))
                .color(Color.ofRgb(0xAAAAAA))
                .margins(Insets.top(3))
        );
    }
    
    private String formatCount(long count) {
        if (count >= 1_000_000) return String.format("%.1fM", count / 1_000_000.0);
        if (count >= 1000) return String.format("%.1fK", count / 1000.0);
        return String.valueOf(count);
    }
}
