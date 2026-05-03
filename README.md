# 🗺️ Player Locator — Fabric Mod for Minecraft 1.21.1

> Track every player on the server — coordinates, dimension, and skin heads!

---

## ✨ Features

| Feature | Description |
|---|---|
| **Player GUI** | Press **`` ` ``** to open a sleek floating overlay |
| **Live Coordinates** | Tracks X / Y / Z of every loaded player in real-time |
| **Dimension Badge** | Color-coded pill showing Overworld 🟢, Nether 🔴, or The End 🟣 |
| **Search** | Filter players by name instantly |
| **Skin Heads** | Every row shows the player's actual face from their skin |
| **Tab-list Skin Heads** | Replaces the latency-dot on Tab with the player's skin head |
| **Draggable Panel** | Drag the GUI window anywhere on screen by its header |
| **Scrollable List** | Smooth scroll through all online players |

---

## 🎮 Controls

| Key | Action |
|---|---|
| `` ` `` (backtick) | Open / Close the Player Locator GUI |
| `Esc` | Close the GUI |
| Mouse drag (header) | Move the window |
| Mouse wheel | Scroll the player list |

You can rebind the key in **Options → Controls → Player Locator**.

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft **1.21.1**
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `playerlocator-1.0.0.jar` into your `.minecraft/mods/` folder
4. Launch and enjoy!

---

## 🔨 Building from Source

```bash
git clone https://github.com/yourusername/playerlocator
cd playerlocator
./gradlew build
# Output JAR → build/libs/playerlocator-1.0.0.jar
```

Requires **Java 21** and **Gradle 8.8+**.

### GitHub Actions

Every push to `main` triggers an automatic build.  
Tagging a commit as `v1.0.0` creates a GitHub Release with the compiled JAR attached.

---

## 🗂️ Project Structure

```
src/
├── main/
│   └── resources/
│       ├── fabric.mod.json
│       ├── playerlocator.mixins.json
│       └── assets/playerlocator/
│           └── lang/en_us.json
└── client/
    └── java/com/playerlocator/
        ├── client/
        │   ├── PlayerLocatorClient.java   ← entrypoint + keybind
        │   ├── PlayerEntry.java           ← player data model
        │   └── SkinHeadRenderer.java      ← face-from-skin renderer
        ├── gui/
        │   └── PlayerLocatorScreen.java   ← full GUI screen
        └── mixin/
            └── PlayerTabListMixin.java    ← replaces tab dots with skin heads
```

---

## 📄 License

MIT — free to use, fork, and modify.
