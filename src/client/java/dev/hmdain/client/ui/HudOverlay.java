package dev.hmdain.client.ui;

import dev.hmdain.client.config.ModConfig;
import dev.hmdain.client.logic.ClientState;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class HudOverlay implements HudRenderCallback {

    @Override
    public void onHudRender(GuiGraphics context, DeltaTracker tickCounter) {
        ClientState state = ClientState.getInstance();
        ModConfig config = ModConfig.get();
        
        if (!config.hudEnabled || state.getResults().isEmpty()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.gui.getDebugOverlay().showDebugScreen() || client.screen != null) {
            return; 
        }

        int x = 10;
        int y = 10;
        int lineHeight = 16;

        // Draw header
        context.drawString(client.font, Component.literal("Materials Needed:"), x, y, 0xFFFFFF);
        y += lineHeight;

        var results = state.getResults();
        int maxLines = config.maxHudLines;
        int currentLine = 0;

        for (Map.Entry<Item, Long> entry : results.entrySet()) {
            if (currentLine >= maxLines) break;

            Item item = entry.getKey();
            Long count = entry.getValue();
            ItemStack stack = item.getDefaultInstance();
            
            context.renderItem(stack, x, y);
            
            String text = count + "x " + stack.getHoverName().getString();
            context.drawString(client.font, text, x + 18, y + 4, 0xFFFFFF);
            
            y += lineHeight;
            currentLine++;
        }
    }
}
