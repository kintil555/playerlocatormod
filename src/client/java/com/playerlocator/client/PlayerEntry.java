package com.playerlocator.client;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlayerEntry {

    public final String name;
    public final double x, y, z;
    public final String dimension;
    public final boolean isLocal;
    public final Identifier skinTexture;

    // Dimension display names
    public static final String DIM_OVERWORLD = "Overworld";
    public static final String DIM_NETHER    = "Nether";
    public static final String DIM_END       = "The End";

    public PlayerEntry(String name, double x, double y, double z,
                       String dimension, boolean isLocal, Identifier skinTexture) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.isLocal = isLocal;
        this.skinTexture = skinTexture;
    }

    /**
     * Build a PlayerEntry from a living PlayerEntity visible to the client.
     */
    public static PlayerEntry fromEntity(PlayerEntity player, boolean isLocal) {
        String dim = getDimensionName(player.getWorld());
        Identifier skin = player.getSkinTextures().texture();
        return new PlayerEntry(
            player.getName().getString(),
            player.getX(),
            player.getY(),
            player.getZ(),
            dim,
            isLocal,
            skin
        );
    }

    /**
     * Build a PlayerEntry from a tab-list entry (player not loaded in world).
     * Coordinates will be 0/0/0 with unknown dimension.
     */
    public static PlayerEntry fromTabEntry(PlayerListEntry entry, Identifier skin) {
        return new PlayerEntry(
            entry.getProfile().getName(),
            0, 0, 0,
            "Unknown",
            false,
            skin
        );
    }

    private static String getDimensionName(World world) {
        net.minecraft.registry.RegistryKey<World> key = world.getRegistryKey();
        if (key == World.OVERWORLD) return DIM_OVERWORLD;
        if (key == World.NETHER)    return DIM_NETHER;
        if (key == World.END)       return DIM_END;
        return key.getValue().getPath();
    }

    /** Formatted coordinate string */
    public String coordsString() {
        return String.format("X: %.0f  Y: %.0f  Z: %.0f", x, y, z);
    }

    /** Dimension color: Overworld = green, Nether = red, End = purple, else gray */
    public int dimensionColor() {
        return switch (dimension) {
            case DIM_OVERWORLD -> 0xFF55FF55;
            case DIM_NETHER    -> 0xFFFF5555;
            case DIM_END       -> 0xFFFF55FF;
            default            -> 0xFFAAAAAA;
        };
    }
}
