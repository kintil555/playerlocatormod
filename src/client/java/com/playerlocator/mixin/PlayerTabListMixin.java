package com.playerlocator.mixin;

import com.playerlocator.client.SkinHeadRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the ping/latency dot in the tab-list with the player's skin head.
 *
 * In Minecraft 1.21.1 (Yarn), PlayerListHud has a private method that renders
 * the connection latency icon next to each player name. We cancel it and draw
 * a skin head instead.
 *
 * If the mixin target method name changes between MC versions, set
 * require = 0 in @Inject so the game still loads (head icons just won't show).
 */
@Mixin(PlayerListHud.class)
public abstract class PlayerTabListMixin {

    @Inject(
        method = "renderLatencyIcon",
        at = @At("HEAD"),
        cancellable = true,
        require = 0   // Don't crash if method is renamed/removed
    )
    private void onRenderLatencyIcon(DrawContext context,
                                     int width,
                                     int x, int y,
                                     PlayerListEntry entry,
                                     CallbackInfo ci) {
        try {
            Identifier skin = entry.getSkinTextures().texture();
            // Draw 10×10 skin head in place of the latency dot
            SkinHeadRenderer.drawWithBorder(context, skin, x, y - 1, 10, 0x55FFFFFF);
            ci.cancel();
        } catch (Exception ignored) {
            // Fallback: let vanilla render the original icon
        }
    }
}
