package com.playerlocator.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Renders the face (head front) portion of a Minecraft skin texture.
 * The face is at UV (8,8)-(16,16) on a 64x64 skin texture.
 * The hat overlay is at UV (40,8)-(48,16).
 */
public class SkinHeadRenderer {

    // Skin texture dimensions (standard 64x64)
    private static final int SKIN_W = 64;
    private static final int SKIN_H = 64;

    // Face region in texture pixels
    private static final int FACE_U = 8, FACE_V = 8;
    private static final int FACE_W = 8, FACE_H = 8;

    // Hat overlay region
    private static final int HAT_U = 40, HAT_V = 8;
    private static final int HAT_W = 8,  HAT_H = 8;

    /**
     * Draw a player head (face + hat) at screen position (x, y) with given size.
     *
     * @param context  DrawContext
     * @param texture  Player skin Identifier
     * @param x        Screen X (top-left)
     * @param y        Screen Y (top-left)
     * @param size     Square size in pixels to render (e.g. 16 or 24)
     */
    public static void draw(DrawContext context, Identifier texture, int x, int y, int size) {
        // Draw base face layer
        context.drawTexture(
            texture,
            x, y,                                     // screen x, y
            size, size,                               // rendered w, h
            FACE_U, FACE_V,                           // texture region u, v
            FACE_W, FACE_H,                           // texture region w, h
            SKIN_W, SKIN_H                            // total texture w, h
        );
        // Draw hat/overlay layer on top
        context.drawTexture(
            texture,
            x, y,
            size, size,
            HAT_U, HAT_V,
            HAT_W, HAT_H,
            SKIN_W, SKIN_H
        );
    }

    /**
     * Draw a small circular-clipped head (square with border highlight).
     * Used for the tab/HUD icons.
     */
    public static void drawWithBorder(DrawContext context, Identifier texture,
                                      int x, int y, int size, int borderColor) {
        // Border
        context.fill(x - 1, y - 1, x + size + 1, y + size + 1, borderColor);
        draw(context, texture, x, y, size);
    }
}
