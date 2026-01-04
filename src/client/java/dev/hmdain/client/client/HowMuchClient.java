package dev.hmdain.client.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.hmdain.client.ui.HowMuchScreen;
import dev.hmdain.client.ui.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class HowMuchClient implements ClientModInitializer {

    private static KeyMapping OPEN_GUI_KEYBINDING;

    @Override
    public void onInitializeClient() {
        OPEN_GUI_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.how-much-do-i-actually-need.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.how-much-do-i-actually-need.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI_KEYBINDING.consumeClick()) {
                Minecraft.getInstance().setScreen(new HowMuchScreen());
            }
        });
        
        HudRenderCallback.EVENT.register(new HudOverlay());
    }
}
