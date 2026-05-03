package com.playerlocator.mixin;

import com.playerlocator.client.SkinHeadRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabListMixin {

    @Inject(
        method = "renderPingIcon",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void onRenderPingIcon(GuiGraphics graphics,
                                  int width,
                                  int x, int y,
                                  PlayerInfo entry,
                                  CallbackInfo ci) {
        try {
            ResourceLocation skin = entry.getSkin().texture();
            if (skin == null) return;
            SkinHeadRenderer.drawWithBorder(graphics, skin, x, y - 1, 10, 0x55FFFFFF);
            ci.cancel();
        } catch (Exception ignored) {}
    }
}
