package com.playerlocator.client;

import com.playerlocator.gui.PlayerLocatorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class PlayerLocatorClient implements ClientModInitializer {

    public static KeyMapping openGuiKey;

    @Override
    public void onInitializeClient() {
        // Register keybinding: backtick (`) to open GUI
        // In 1.21.9+, KeyMapping constructor requires KeyMapping.Category instead of String.
        // Use MISC_CATEGORY as the category.
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.playerlocator.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT, // ` key
            KeyMapping.MISC_CATEGORY
        ));

        // Tick event to detect key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.consumeClick()) {
                if (client.player != null && client.screen == null) {
                    client.setScreen(new PlayerLocatorScreen());
                }
            }
        });
    }
}
