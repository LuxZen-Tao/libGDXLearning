# libGDX Learning

A libGDX learning project with a scene2d UI skeleton and a Two Point Hospital-style room-placement world builder.

## Project Structure

```
libGDXLearning/
├── core/                           # Core game code (platform-independent)
│   ├── assets/
│   │   └── maps/                   # Tiled map assets (optional)
│   └── src/main/java/com/libgdxlearning/
│       ├── MainGame.java           # Entry point – sets up AlivePackScreen
│       ├── AlivePackScreen.java    # Main screen: HUD, drawers, toasts, sim controls, world view
│       ├── SimState.java           # Game state (cash, rep, chaos, morale, time)
│       ├── ToastManager.java       # Animated toast notifications
│       ├── world/
│       │   ├── Grid.java           # 2-D tile grid (width × height cells)
│       │   └── tiles/
│       │       ├── TileType.java   # Tile enum: EMPTY, FLOOR_WOOD, FLOOR_KITCHEN, WALL_WOOD, DOOR_WOOD
│       │       └── TileCell.java   # Single cell (type + blocked flag)
│       ├── rooms/
│       │   ├── RoomType.java       # Room enum: MAIN_BAR, KITCHEN, TOILETS, MANAGER_OFFICE, BEER_GARDEN
│       │   ├── RoomTemplate.java   # Template dimensions + tile-type helpers
│       │   └── RoomPlacementSystem.java  # canPlaceAt / placeAt logic; holds activeTemplate & hover coords
│       ├── render/
│       │   └── WorldView.java      # Scene2D Actor – renders grid + placed rooms + hover preview
│       └── input/
│           └── WorldInput.java     # InputAdapter – hover tracking + click-to-place
├── desktop/
│   ├── src/main/java/com/libgdxlearning/
│   │   └── DesktopLauncher.java    # Desktop entry point (800×600, 60 FPS)
│   └── src/main/resources/assets/ # UI skin files (uiskin.json/atlas/png, fonts)
├── build.gradle
└── settings.gradle
```

## Requirements

- Java 8 or higher
- Gradle (included via wrapper)

## Building the Project

```bash
./gradlew build
```

## Running the Project

```bash
./gradlew desktop:run
```

This launches an 800×600 window with:
- **Top HUD** – day/time clock, cash, rep, chaos, morale (with flash-on-change animations)
- **Bottom bar** – pause, speed (x1/x2/x4), and stat-manipulation buttons
- **Side drawers** – "Feed" (left) and "Manage" (right), slide in/out with animation
- **Toast notifications** – fade-in/slide-out messages
- **World view (center)** – scrollable grid; hover shows a semi-transparent room preview (green = valid, red = blocked); click to place

### Keyboard Shortcuts

| Key      | Action            |
|----------|-------------------|
| `Space`  | Pause / Resume    |
| `1`      | Speed x1          |
| `2`      | Speed x2          |
| `3`      | Speed x4          |

## World Builder

The center area renders a tile grid backed by a `Grid` (80 × 60 cells).

### Changing the active room template

Open `RoomPlacementSystem.java` and change the constructor line:

```java
activeTemplate = new RoomTemplate(RoomType.MAIN_BAR, 10, 8);
```

Swap `RoomType.MAIN_BAR` for any value in `RoomType` (e.g. `KITCHEN`, `TOILETS`) and adjust the width/height.

### Where the world view is inserted

In `AlivePackScreen.show()`, after the top HUD and bottom controls are built, the world view fills the center cell of the root Table:

```java
root.add(worldView).expand().fill().row();
```

This replaces what was previously an empty spacer row (`root.add().expand().fill().row()`).

Input is handled by an `InputMultiplexer` — the Stage (UI) processes events first; only clicks that land inside the `WorldView` actor (and not on any UI widget) reach `WorldInput` and trigger placement.

## Resources

- [libGDX Wiki](https://libgdx.com/wiki/)
- [libGDX API Documentation](https://libgdx.com/dev/api/)
- [libGDX Community](https://discord.gg/6pgDK9F)
