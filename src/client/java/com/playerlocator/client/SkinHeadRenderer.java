package com.playerlocator.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the face (head front) portion of a Minecraft skin texture.
 *
 * Standard 64x64 skin layout:
 *   Face layer:    UV pixel (8,8) size 8x8
 *   Hat overlay:   UV pixel (40,8) size 8x8
 */
public class SkinHeadRenderer {

    private static final int SKIN_W = 64;
    private static final int SKIN_H = 64;

    /**
     * Draw a player head (face + hat overlay) at screen position (x, y) with given size.
     */
    public static void draw(GuiGraphics graphics, ResourceLocation texture, int x, int y, int size) {
        // Face layer: pixel region starting at (8,8), 8x8 pixels on 64x64 texture
        graphics.blit(texture, x, y, size, size, 8f, 8f, 8, 8, SKIN_W, SKIN_H);
        // Hat overlay: pixel region starting at (40,8), 8x8 pixels on 64x64 texture
        graphics.blit(texture, x, y, size, size, 40f, 8f, 8, 8, SKIN_W, SKIN_H);
    }

    /**
     * Draw head with a 1-pixel colored border around it.
     */
    public static void drawWithBorder(GuiGraphics graphics, ResourceLocation texture,
                                      int x, int y, int size, int borderColor) {
        graphics.fill(x - 1, y - 1, x + size + 1, y + size + 1, borderColor);
        draw(graphics, texture, x, y, size);
    }
}
