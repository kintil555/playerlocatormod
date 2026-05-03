package com.playerlocator.gui;

import com.playerlocator.client.PlayerEntry;
import com.playerlocator.client.SkinHeadRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerLocatorScreen extends Screen {

    private static final int PANEL_W = 360;
    private static final int PANEL_H = 260;
    private static final int HEAD_SIZE = 20;
    private static final int ROW_H = 28;
    private static final int INNER_PAD = 10;
    private static final int SCROLL_BAR_W = 6;

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

    private EditBox searchField;
    private List<PlayerEntry> allPlayers      = new ArrayList<>();
    private List<PlayerEntry> filteredPlayers = new ArrayList<>();

    private int scrollOffset = 0;
    private int hoveredRow   = -1;

    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    public PlayerLocatorScreen() {
        super(Component.translatable("screen.playerlocator.title"));
    }

    @Override
    protected void init() {
        panelX = (width  - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;

        int sfX = panelX + INNER_PAD;
        int sfY = panelY + 34;
        int sfW = PANEL_W - INNER_PAD * 2 - 4;

        searchField = new EditBox(font, sfX, sfY, sfW, 18,
            Component.translatable("gui.playerlocator.search"));
        searchField.setMaxLength(32);
        searchField.setHint(Component.literal("Search player..."));
        searchField.setResponder(s -> {
            scrollOffset = 0;
            applyFilter(s);
        });
        addRenderableWidget(searchField);

        refreshPlayers();
        applyFilter("");
    }

    private void refreshPlayers() {
        allPlayers.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        List<String> addedNames = new ArrayList<>();

        for (Player p : mc.level.players()) {
            boolean isLocal = p.getUUID().equals(mc.player.getUUID());
            PlayerEntry entry = PlayerEntry.fromEntity(p, isLocal);
            allPlayers.add(entry);
            addedNames.add(p.getName().getString());
        }

        ClientPacketListener net = mc.getConnection();
        if (net != null) {
            for (PlayerInfo tab : net.getOnlinePlayers()) {
                String name = tab.getProfile().getName();
                if (addedNames.contains(name)) continue;
                ResourceLocation skin = null;
                try { skin = tab.getSkin().texture(); } catch (Exception ignored) {}
                allPlayers.add(PlayerEntry.fromTabEntry(tab, skin));
            }
        }

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

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderTransparentBackground(graphics);
        drawPanel(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderTransparentBackground(graphics);
    }

    private void drawPanel(GuiGraphics g, int mx, int my) {
        g.fill(panelX + 4, panelY + 4, panelX + PANEL_W + 4, panelY + PANEL_H + 4, 0x66000000);
        g.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, COL_BG);
        drawBorder(g, panelX, panelY, PANEL_W, PANEL_H, COL_BORDER);

        g.fill(panelX, panelY, panelX + PANEL_W, panelY + 28, COL_HEADER);

        String title = "Player Locator";
        int titleW = font.width(title);
        g.drawString(font, title, panelX + (PANEL_W - titleW) / 2, panelY + 10, COL_ACCENT, false);

        String countStr = filteredPlayers.size() + " players";
        g.drawString(font, countStr, panelX + PANEL_W - font.width(countStr) - INNER_PAD, panelY + 10, 0xFF667799, false);

        g.drawString(font, "x", panelX + PANEL_W - 14, panelY + 5, 0xFFFF5555, false);

        g.fill(panelX, panelY + 28, panelX + PANEL_W, panelY + 29, COL_ACCENT);

        int listY = panelY + 60;
        int listH = PANEL_H - 70;
        int listW = PANEL_W - SCROLL_BAR_W - 4;

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

            drawRow(g, entry, panelX, rowY, listW, ROW_H, idx, isHovered);
        }

        if (filteredPlayers.size() > visibleRows) {
            drawScrollbar(g, panelX + PANEL_W - SCROLL_BAR_W - 2,
                listY, SCROLL_BAR_W, listH, filteredPlayers.size(), visibleRows);
        }

        g.fill(panelX, panelY + PANEL_H - 18, panelX + PANEL_W, panelY + PANEL_H, COL_HEADER);
        String hint = "Scroll to browse  |  Press ` to close";
        g.drawString(font, hint,
            panelX + (PANEL_W - font.width(hint)) / 2,
            panelY + PANEL_H - 12, 0xFF445566, false);
    }

    private void drawRow(GuiGraphics g, PlayerEntry e,
                         int rowX, int rowY, int rowW, int rowH,
                         int idx, boolean hovered) {
        int bg = hovered ? COL_ROW_HOVER : (idx % 2 == 0 ? COL_ROW_EVEN : COL_ROW_ODD);
        g.fill(rowX, rowY, rowX + rowW, rowY + rowH, bg);
        g.fill(rowX, rowY + rowH - 1, rowX + rowW, rowY + rowH, COL_BORDER);

        int cx = rowX + INNER_PAD;

        int headY = rowY + (rowH - HEAD_SIZE) / 2;
        int borderColor = e.isLocal ? 0xFFFFAA00 : COL_BORDER;
        SkinHeadRenderer.drawWithBorder(g, e.skinTexture, cx, headY, HEAD_SIZE, borderColor);
        cx += HEAD_SIZE + 8;

        int nameColor = e.isLocal ? 0xFFFFCC44 : COL_TEXT;
        g.drawString(font, e.name, cx, rowY + 4, nameColor, false);

        if (e.isLocal) {
            int badgeX = cx + font.width(e.name) + 4;
            g.fill(badgeX, rowY + 3, badgeX + 26, rowY + 13, COL_LOCAL_BADGE);
            g.drawString(font, "YOU", badgeX + 3, rowY + 4, 0xFFAAFFAA, false);
        }

        g.drawString(font, e.coordsString(), cx, rowY + 16, COL_COORDS, false);

        String dim = e.dimension;
        int dimW = font.width(dim) + 8;
        int dimX = rowX + rowW - dimW - INNER_PAD;
        int dimY = rowY + (rowH - 11) / 2;
        g.fill(dimX - 2, dimY - 1, dimX + dimW, dimY + 10, 0x66000000);
        drawBorder(g, dimX - 2, dimY - 1, dimW + 2, 12, e.dimensionColor());
        g.drawString(font, dim, dimX + 2, dimY + 1, e.dimensionColor(), false);
    }

    private void drawScrollbar(GuiGraphics g, int x, int y,
                               int w, int h, int total, int visible) {
        g.fill(x, y, x + w, y + h, COL_SCROLL_BG);
        float ratio = (float) visible / total;
        int barH   = Math.max(16, (int)(h * ratio));
        int barY   = y + (int)((h - barH) * ((float) scrollOffset / Math.max(1, total - visible)));
        g.fill(x + 1, barY, x + w - 1, barY + barH, COL_SCROLL_FG);
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,         x + w,     y + 1,     color);
        g.fill(x,         y + h - 1, x + w,     y + h,     color);
        g.fill(x,         y,         x + 1,     y + h,     color);
        g.fill(x + w - 1, y,         x + w,     y + h,     color);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx >= panelX + PANEL_W - 16 && mx <= panelX + PANEL_W - 2
         && my >= panelY + 2 && my <= panelY + 14) {
            onClose();
            return true;
        }
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
            panelX = Math.max(0, Math.min(panelX, width  - PANEL_W));
            panelY = Math.max(0, Math.min(panelY, height - PANEL_H));
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
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
         || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_GRAVE_ACCENT) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        refreshPlayers();
        applyFilter(searchField.getValue());
    }
}
