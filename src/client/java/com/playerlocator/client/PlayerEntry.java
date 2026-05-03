package com.playerlocator.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PlayerEntry {

    public final String name;
    public final double x, y, z;
    public final String dimension;
    public final boolean isLocal;
    public final Identifier skinTexture;

    public static final String DIM_OVERWORLD = "Overworld";
    public static final String DIM_NETHER    = "Nether";
    public static final String DIM_END       = "The End";

    // Default Steve skin fallback
    private static final Identifier DEFAULT_SKIN =
        Identifier.of("minecraft", "textures/entity/player/wide/steve.png");

    public PlayerEntry(String name, double x, double y, double z,
                       String dimension, boolean isLocal, Identifier skinTexture) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.isLocal = isLocal;
        this.skinTexture = (skinTexture != null) ? skinTexture : DEFAULT_SKIN;
    }

    /**
     * Build a PlayerEntry from a loaded PlayerEntity.
     * getSkinTextures() is only on AbstractClientPlayerEntity (client-side subclass).
     */
    public static PlayerEntry fromEntity(PlayerEntity player, boolean isLocal) {
        String dim = getDimensionName(player.getWorld());
        Identifier skin = DEFAULT_SKIN;
        try {
            if (player instanceof AbstractClientPlayerEntity clientPlayer) {
                SkinTextures textures = clientPlayer.getSkinTextures();
                if (textures != null && textures.texture() != null) {
                    skin = textures.texture();
                }
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
    public static PlayerEntry fromTabEntry(PlayerListEntry entry, Identifier skin) {
        Identifier safeSkin = DEFAULT_SKIN;
        try {
            SkinTextures textures = entry.getSkinTextures();
            if (textures != null && textures.texture() != null) {
                safeSkin = textures.texture();
            }
        } catch (Exception ignored) {
            // Use default skin
        }
        return new PlayerEntry(
            entry.getProfile().getName(),
            0, 0, 0,
            "Unknown",
            false,
            safeSkin
        );
    }

    private static String getDimensionName(World world) {
        net.minecraft.registry.RegistryKey<World> key = world.getRegistryKey();
        if (key == World.OVERWORLD) return DIM_OVERWORLD;
        if (key == World.NETHER)    return DIM_NETHER;
        if (key == World.END)       return DIM_END;
        return key.getValue().getPath();
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
