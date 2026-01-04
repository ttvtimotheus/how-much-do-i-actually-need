package dev.hmdain.client.ui;

import dev.hmdain.client.logic.ClientState;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HowMuchScreen extends BaseOwoScreen<FlowLayout> {

    private final ClientState state = ClientState.getInstance();
    private FlowLayout targetListContainer;
    private FlowLayout resultListContainer;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        // Title
        rootComponent.child(Components.label(Component.literal("How Much Do I Actually Need?")).margins(Insets.bottom(10)));

        // Main Container - Horizontal Flow
        var mainContainer = Containers.horizontalFlow(Sizing.fill(90), Sizing.fill(80));
        mainContainer.gap(10);

        // --- Left Side: Search & Item List ---
        var leftPanel = Containers.verticalFlow(Sizing.fill(40), Sizing.fill(100));
        leftPanel.gap(5);

        var searchField = Components.textBox(Sizing.fill(100));
        // Placeholder text is set via the text box itself
        
        var itemScrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), Containers.verticalFlow(Sizing.fill(100), Sizing.content()));
        var itemList = itemScrollContainer.child();
        itemList.padding(Insets.of(5));
        
        // Initial population
        populateItemList(itemList, "");
        searchField.onChanged().subscribe(s -> populateItemList(itemList, s));

        leftPanel.child(searchField);
        leftPanel.child(itemScrollContainer);


        // --- Right Side: Targets & Results ---
        var rightPanel = Containers.verticalFlow(Sizing.fill(60), Sizing.fill(100));
        rightPanel.gap(5);

        // Targets Section
        rightPanel.child(Components.label(Component.literal("Targets")).margins(Insets.bottom(2)));
        var targetScroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(40), Containers.verticalFlow(Sizing.fill(100), Sizing.content()));
        targetScroll.surface(Surface.PANEL_INSET);
        targetListContainer = targetScroll.child();
        targetListContainer.padding(Insets.of(5));

        // Results Section
        rightPanel.child(Components.label(Component.literal("Required Materials")).margins(Insets.top(10).bottom(2)));
        var resultScroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(50), Containers.verticalFlow(Sizing.fill(100), Sizing.content()));
        resultScroll.surface(Surface.PANEL_INSET);
        resultListContainer = resultScroll.child();
        resultListContainer.padding(Insets.of(5));

        rightPanel.child(targetScroll);
        rightPanel.child(resultScroll);

        mainContainer.child(leftPanel);
        mainContainer.child(rightPanel);

        rootComponent.child(mainContainer);
    }
    
    private void populateItemList(FlowLayout container, String search) {
        container.clearChildren();
        var registry = BuiltInRegistries.ITEM;
        String lowerSearch = search.toLowerCase();
        
        registry.stream()
                .filter(item -> search.isEmpty() || item.getDefaultInstance().getHoverName().getString().toLowerCase().contains(lowerSearch))
                .limit(50) // Limit to avoid lag
                .forEach(item -> {
                    var itemRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
                    itemRow.verticalAlignment(VerticalAlignment.CENTER);
                    itemRow.gap(5);
                    
                    itemRow.child(Components.item(item.getDefaultInstance()));
                    itemRow.child(Components.label(item.getDefaultInstance().getHoverName()));
                    
                    var spacer = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1));
                    itemRow.child(spacer);

                    var addButton = Components.button(Component.literal("+"), button -> {
                        addTarget(item);
                    });
                    addButton.sizing(Sizing.fixed(16), Sizing.fixed(16));
                    
                    itemRow.child(addButton);
                    container.child(itemRow);
                });
    }

    private void addTarget(Item item) {
        state.addTarget(item);
        updateUI();
    }

    private void removeTarget(ItemStack stack) {
        state.removeTarget(stack);
        updateUI();
    }

    private void updateUI() {
        // Update Targets UI
        targetListContainer.clearChildren();
        for (ItemStack stack : state.getTargets()) {
            var row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
            row.verticalAlignment(VerticalAlignment.CENTER);
            row.gap(5);
            
            row.child(Components.item(stack));
            row.child(Components.label(Component.literal(stack.getCount() + "x " + stack.getHoverName().getString())));
            
            var spacer = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1));
            row.child(spacer);

            var removeBtn = Components.button(Component.literal("x"), button -> removeTarget(stack));
            removeBtn.sizing(Sizing.fixed(16), Sizing.fixed(16));
            
            row.child(removeBtn);
            targetListContainer.child(row);
        }

        var results = state.getResults();

        // Update Results UI
        resultListContainer.clearChildren();
        results.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // Sort descending by count
                .forEach(entry -> {
                    var item = entry.getKey();
                    var count = entry.getValue();
                    
                    var row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
                    row.verticalAlignment(VerticalAlignment.CENTER);
                    row.gap(5);
                    
                    row.child(Components.item(item.getDefaultInstance()));
                    row.child(Components.label(Component.literal(count + "x " + item.getDefaultInstance().getHoverName().getString())));
                    
                    resultListContainer.child(row);
                });
    }
}
