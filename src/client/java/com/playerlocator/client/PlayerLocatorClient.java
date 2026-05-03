package com.playerlocator.client;

import com.playerlocator.gui.PlayerLocatorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PlayerLocatorClient implements ClientModInitializer {

    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        // Register keybinding: backtick (`) to open GUI
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.playerlocator.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT, // ` key
            "category.playerlocator"
        ));

        // Tick event to detect key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new PlayerLocatorScreen());
                }
            }
        });
    }
}
