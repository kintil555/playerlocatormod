package com.playerlocator.gui;

import com.playerlocator.client.PlayerEntry;
import com.playerlocator.client.SkinHeadRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PlayerLocatorScreen extends Screen {

    // Layout constants
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 260;
    private static final int HEAD_SIZE = 20;
    private static final int ROW_H = 28;
    private static final int INNER_PAD = 10;
    private static final int SCROLL_BAR_W = 6;

    // Colors (ARGB)
    private static final int COL_BG          = 0xE5101318;
    private static final int COL_HEADER      = 0xFF1A2030;
    private static final int COL_ROW_EVEN    = 0xFF12181F;
    private static final int COL_ROW_ODD     = 0xFF0E141A;
    private static final int COL_ROW_HOVER   = 0xFF1E3048;
    private static final int COL_ACCENT      = 0xFF3A8FFF;
    private static final int COL_BORDER      = 0xFF2A3A50;
    private static final int COL_TEXT        = 0xFFE0E8FF;
    private static final int COL_COORDS      = 0xFFAACCFF;
    private static final int COL_SCROLL_BG   = 0xFF0A0E14;
    private static final int COL_SCROLL_FG   = 0xFF2A5080;
    private static final int COL_LOCAL_BADGE = 0xFF1A7A2A;

    private int panelX, panelY;

    private TextFieldWidget searchField;
    private List<PlayerEntry> allPlayers  = new ArrayList<>();
    private List<PlayerEntry> filteredPlayers = new ArrayList<>();

    private int scrollOffset = 0;
    private int hoveredRow   = -1;

    // Dragging
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    public PlayerLocatorScreen() {
        super(Text.translatable("screen.playerlocator.title"));
    }

    @Override
    protected void init() {
        panelX = (width  - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;

        // Search field — positioned inside header bar
        int sfX = panelX + INNER_PAD;
        int sfY = panelY + 34;
        int sfW = PANEL_W - INNER_PAD * 2 - 4;

        searchField = new TextFieldWidget(
            textRenderer,
            sfX, sfY, sfW, 18,
            Text.translatable("gui.playerlocator.search")
        );
        searchField.setMaxLength(32);
        searchField.setPlaceholder(Text.literal("Search player..."));
        searchField.setChangedListener(s -> {
            scrollOffset = 0;
            applyFilter(s);
        });
        addDrawableChild(searchField);

        refreshPlayers();
        applyFilter("");
    }

    // ── Data gathering ───────────────────────────────────────────────────────

    private void refreshPlayers() {
        allPlayers.clear();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        // Build a set of names already added from world entities
        List<String> addedNames = new ArrayList<>();

        // Prefer actual loaded entities (have real coordinates + dimension)
        for (PlayerEntity p : mc.world.getPlayers()) {
            boolean isLocal = p.getUuid().equals(mc.player.getUuid());
            PlayerEntry entry = PlayerEntry.fromEntity(p, isLocal);
            allPlayers.add(entry);
            addedNames.add(p.getName().getString());
        }

        // Fill remaining from tab list (offline/distant players on servers)
        ClientPlayNetworkHandler net = mc.getNetworkHandler();
        if (net != null) {
            for (PlayerListEntry tab : net.getPlayerList()) {
                String name = tab.getProfile().getName();
                if (addedNames.contains(name)) continue;
                // getSkinTextures() is available on PlayerListEntry directly
                Identifier skin = tab.getSkinTextures().texture();
                allPlayers.add(PlayerEntry.fromTabEntry(tab, skin));
            }
        }

        // Sort: local player first, then alphabetical
        allPlayers.sort((a, b) -> {
            if (a.isLocal) return -1;
            if (b.isLocal) return 1;
            return a.name.compareToIgnoreCase(b.name);
        });
    }

    private void applyFilter(String query) {
        filteredPlayers.clear();
        String q = query.trim().toLowerCase();
        for (PlayerEntry e : allPlayers) {
            if (q.isEmpty() || e.name.toLowerCase().contains(q)) {
                filteredPlayers.add(e);
            }
        }
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render darkened backdrop (vanilla method signature for 1.21.1)
        this.renderBackground(context, mouseX, mouseY, delta);

        drawPanel(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawPanel(DrawContext ctx, int mx, int my) {
        // Shadow
        ctx.fill(panelX + 4, panelY + 4, panelX + PANEL_W + 4, panelY + PANEL_H + 4, 0x66000000);

        // Background
        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, COL_BG);
        drawBorder(ctx, panelX, panelY, PANEL_W, PANEL_H, COL_BORDER);

        // Header bar
        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + 28, COL_HEADER);

        // Title
        String title = "Player Locator";
        int titleW = textRenderer.getWidth(title);
        ctx.drawText(textRenderer, title, panelX + (PANEL_W - titleW) / 2, panelY + 10, COL_ACCENT, false);

        // Player count badge
        String countStr = filteredPlayers.size() + " players";
        ctx.drawText(textRenderer, countStr, panelX + PANEL_W - textRenderer.getWidth(countStr) - INNER_PAD, panelY + 10, 0xFF667799, false);

        // Close button [X]
        ctx.drawText(textRenderer, "✕", panelX + PANEL_W - 14, panelY + 5, 0xFFFF5555, false);

        // Separator under header
        ctx.fill(panelX, panelY + 28, panelX + PANEL_W, panelY + 29, COL_ACCENT);

        // Search field area (drawn by super, just draw its label)
        // Player list area starts at panelY + 60
        int listY = panelY + 60;
        int listH = PANEL_H - 70;
        int listW = PANEL_W - SCROLL_BAR_W - 4;

        // Clip + draw rows
        int visibleRows = listH / ROW_H;
        int maxScroll   = Math.max(0, filteredPlayers.size() - visibleRows);
        scrollOffset    = Math.max(0, Math.min(scrollOffset, maxScroll));

        hoveredRow = -1;
        for (int i = 0; i < visibleRows; i++) {
            int idx = i + scrollOffset;
            if (idx >= filteredPlayers.size()) break;

            PlayerEntry entry = filteredPlayers.get(idx);
            int rowY = listY + i * ROW_H;

            boolean isHovered = mx >= panelX && mx < panelX + listW
                             && my >= rowY   && my < rowY + ROW_H;
            if (isHovered) hoveredRow = idx;

            drawRow(ctx, entry, panelX, rowY, listW, ROW_H, idx, isHovered);
        }

        // Scrollbar
        if (filteredPlayers.size() > visibleRows) {
            drawScrollbar(ctx, panelX + PANEL_W - SCROLL_BAR_W - 2,
                listY, SCROLL_BAR_W, listH, filteredPlayers.size(), visibleRows);
        }

        // Footer
        ctx.fill(panelX, panelY + PANEL_H - 18, panelX + PANEL_W, panelY + PANEL_H, COL_HEADER);
        String hint = "Scroll to browse  •  Press ` to close";
        ctx.drawText(textRenderer, hint,
            panelX + (PANEL_W - textRenderer.getWidth(hint)) / 2,
            panelY + PANEL_H - 12, 0xFF445566, false);
    }

    private void drawRow(DrawContext ctx, PlayerEntry e,
                         int rowX, int rowY, int rowW, int rowH,
                         int idx, boolean hovered) {
        // Alternating background + hover
        int bg = hovered ? COL_ROW_HOVER : (idx % 2 == 0 ? COL_ROW_EVEN : COL_ROW_ODD);
        ctx.fill(rowX, rowY, rowX + rowW, rowY + rowH, bg);

        // Separator line
        ctx.fill(rowX, rowY + rowH - 1, rowX + rowW, rowY + rowH, COL_BORDER);

        int cx = rowX + INNER_PAD;

        // Skin head (20×20), vertically centered
        int headY = rowY + (rowH - HEAD_SIZE) / 2;
        int borderColor = e.isLocal ? 0xFFFFAA00 : COL_BORDER;
        SkinHeadRenderer.drawWithBorder(ctx, e.skinTexture, cx, headY, HEAD_SIZE, borderColor);
        cx += HEAD_SIZE + 8;

        // Player name
        String nameDisplay = e.name;
        int nameColor = e.isLocal ? 0xFFFFCC44 : COL_TEXT;
        ctx.drawText(textRenderer, nameDisplay, cx, rowY + 4, nameColor, false);

        // "YOU" badge for local player
        if (e.isLocal) {
            int badgeX = cx + textRenderer.getWidth(nameDisplay) + 4;
            ctx.fill(badgeX, rowY + 3, badgeX + 26, rowY + 13, COL_LOCAL_BADGE);
            ctx.drawText(textRenderer, "YOU", badgeX + 3, rowY + 4, 0xFFAAFFAA, false);
        }

        // Coordinates
        ctx.drawText(textRenderer, e.coordsString(), cx, rowY + 16, COL_COORDS, false);

        // Dimension pill (right side)
        String dim = e.dimension;
        int dimW = textRenderer.getWidth(dim) + 8;
        int dimX = rowX + rowW - dimW - INNER_PAD;
        int dimY = rowY + (rowH - 11) / 2;
        ctx.fill(dimX - 2, dimY - 1, dimX + dimW, dimY + 10, 0x66000000);
        drawBorder(ctx, dimX - 2, dimY - 1, dimW + 2, 12, e.dimensionColor());
        ctx.drawText(textRenderer, dim, dimX + 2, dimY + 1, e.dimensionColor(), false);
    }

    private void drawScrollbar(DrawContext ctx, int x, int y,
                               int w, int h, int total, int visible) {
        ctx.fill(x, y, x + w, y + h, COL_SCROLL_BG);
        float ratio = (float) visible / total;
        int barH   = Math.max(16, (int)(h * ratio));
        int barY   = y + (int)((h - barH) * ((float) scrollOffset / Math.max(1, total - visible)));
        ctx.fill(x + 1, barY, x + w - 1, barY + barH, COL_SCROLL_FG);
    }

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x,         y,         x + w,     y + 1,     color); // top
        ctx.fill(x,         y + h - 1, x + w,     y + h,     color); // bottom
        ctx.fill(x,         y,         x + 1,     y + h,     color); // left
        ctx.fill(x + w - 1, y,         x + w,     y + h,     color); // right
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Close [X] button
        if (mx >= panelX + PANEL_W - 16 && mx <= panelX + PANEL_W - 2
         && my >= panelY + 2 && my <= panelY + 14) {
            close();
            return true;
        }

        // Start drag from header
        if (mx >= panelX && mx < panelX + PANEL_W
         && my >= panelY && my < panelY + 28) {
            dragging = true;
            dragOffsetX = (int) mx - panelX;
            dragOffsetY = (int) my - panelY;
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            panelX = (int) mx - dragOffsetX;
            panelY = (int) my - dragOffsetY;
            // Clamp to screen
            panelX = Math.max(0, Math.min(panelX, width  - PANEL_W));
            panelY = Math.max(0, Math.min(panelY, height - PANEL_H));

            // Reposition search field
            searchField.setX(panelX + INNER_PAD);
            searchField.setY(panelY + 34);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        scrollOffset -= (int) vAmount;
        int maxScroll = Math.max(0, filteredPlayers.size() - ((PANEL_H - 70) / ROW_H));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC or ` closes the screen
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
         || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_GRAVE_ACCENT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false; // keep game running
    }

    // Refresh player list every time screen ticks
    @Override
    public void tick() {
        super.tick();
        refreshPlayers();
        applyFilter(searchField.getText());
    }
}
