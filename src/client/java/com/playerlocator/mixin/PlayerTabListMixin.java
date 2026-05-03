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
 * Replaces the colored dot on the tab/player-list HUD
 * with the actual player skin head.
 *
 * The vanilla method renderLatencyIcon() draws a small dot/icon
 * at the right side of each player row. We inject BEFORE it runs
 * and cancel it, then draw the skin head instead.
 *
 * Note: In 1.21.x the method signature is:
 *   renderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry)
 */
@Mixin(PlayerListHud.class)
public abstract class PlayerTabListMixin {

    /**
     * Inject before the latency icon is rendered.
     * We draw a 10×10 skin head in its place and cancel the original icon.
     */
    @Inject(
        method = "renderLatencyIcon",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderLatencyIcon(DrawContext context,
                                     int width, int x, int y,
                                     PlayerListEntry entry,
                                     CallbackInfo ci) {
        try {
            Identifier skin = entry.getSkinTextures().texture();
            // Draw a 10×10 head, vertically centred at the icon slot (typically 9px tall)
            SkinHeadRenderer.drawWithBorder(context, skin, x, y - 1, 10, 0x66FFFFFF);
            // Cancel the original dot rendering
            ci.cancel();
        } catch (Exception ignored) {
            // If anything fails, let the original icon render
        }
    }
}
