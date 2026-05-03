package com.playerlocator.client;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PlayerEntry {

    public final String name;
    public final double x, y, z;
    public final String dimension;
    public final boolean isLocal;
    public final ResourceLocation skinTexture;

    public static final String DIM_OVERWORLD = "Overworld";
    public static final String DIM_NETHER    = "Nether";
    public static final String DIM_END       = "The End";

    // Default Steve skin fallback
    private static final ResourceLocation DEFAULT_SKIN =
        ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    public PlayerEntry(String name, double x, double y, double z,
                       String dimension, boolean isLocal, ResourceLocation skinTexture) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.isLocal = isLocal;
        this.skinTexture = (skinTexture != null) ? skinTexture : DEFAULT_SKIN;
    }

    /**
     * Build a PlayerEntry from a loaded Player entity.
     */
    public static PlayerEntry fromEntity(Player player, boolean isLocal) {
        String dim = getDimensionName(player.level());
        ResourceLocation skin = DEFAULT_SKIN;
        try {
            if (player instanceof AbstractClientPlayer clientPlayer) {
                // 1.21.x: getSkinTextureLocation() removed, use getSkinTextures().texture()
                ResourceLocation tex = clientPlayer.getSkinTextures().texture();
                if (tex != null) skin = tex;
            }
        } catch (Exception ignored) {
            // Skin not loaded yet, use default
        }
        return new PlayerEntry(
            player.getName().getString(),
            player.getX(), player.getY(), player.getZ(),
            dim, isLocal, skin
        );
    }

    /**
     * Build a PlayerEntry from a tab-list entry (player not in render distance).
     */
    public static PlayerEntry fromTabEntry(PlayerInfo entry, ResourceLocation skin) {
        ResourceLocation safeSkin = DEFAULT_SKIN;
        try {
            if (skin != null) {
                safeSkin = skin;
            } else {
                // 1.21.x: use getSkinTextures().texture() instead of getSkinTextureLocation()
                ResourceLocation tex = entry.getSkinTextures().texture();
                if (tex != null) safeSkin = tex;
            }
        } catch (Exception ignored) {
            // Use default skin
        }
        String playerName;
        try {
            playerName = entry.getProfile().getName();
        } catch (Exception ignored) {
            playerName = "Unknown";
        }
        return new PlayerEntry(
            playerName,
            0, 0, 0,
            "Unknown",
            false,
            safeSkin
        );
    }

    private static String getDimensionName(Level level) {
        var key = level.dimension();
        if (key == Level.OVERWORLD) return DIM_OVERWORLD;
        if (key == Level.NETHER)    return DIM_NETHER;
        if (key == Level.END)       return DIM_END;
        return key.location().getPath();
    }

    public String coordsString() {
        return String.format("X: %.0f  Y: %.0f  Z: %.0f", x, y, z);
    }

    public int dimensionColor() {
        return switch (dimension) {
            case DIM_OVERWORLD -> 0xFF55FF55;
            case DIM_NETHER    -> 0xFFFF5555;
            case DIM_END       -> 0xFFFF55FF;
            default            -> 0xFFAAAAAA;
        };
    }
}
