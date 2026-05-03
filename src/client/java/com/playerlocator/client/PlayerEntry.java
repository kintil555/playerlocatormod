package com.playerlocator.client;

import com.mojang.authlib.GameProfile;
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

    public static PlayerEntry fromEntity(Player player, boolean isLocal) {
        String dim = getDimensionName(player.level());
        ResourceLocation skin = DEFAULT_SKIN;
        try {
            if (player instanceof AbstractClientPlayer clientPlayer) {
                ResourceLocation tex = clientPlayer.getSkin().texture();
                if (tex != null) skin = tex;
            }
        } catch (Exception ignored) {}
        return new PlayerEntry(
            player.getName().getString(),
            player.getX(), player.getY(), player.getZ(),
            dim, isLocal, skin
        );
    }

    public static PlayerEntry fromTabEntry(PlayerInfo entry, ResourceLocation skin) {
        ResourceLocation safeSkin = DEFAULT_SKIN;
        try {
            if (skin != null) {
                safeSkin = skin;
            } else {
                ResourceLocation tex = entry.getSkin().texture();
                if (tex != null) safeSkin = tex;
            }
        } catch (Exception ignored) {}

        String playerName = "Unknown";
        try {
            GameProfile profile = entry.getProfile();
            playerName = profile.getName();
        } catch (Exception ignored) {}

        return new PlayerEntry(playerName, 0, 0, 0, "Unknown", false, safeSkin);
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
