# libGDX Learning

A libGDX learning project with a simple scene2d UI skeleton.

## Project Structure

```
libGDXLearning/
├── core/                           # Core game code (platform-independent)
│   ├── assets/
│   │   └── maps/                   # Tiled map assets (optional)
│   └── src/main/java/com/libgdxlearning/
│       ├── MainGame.java           # Entry point – sets up AlivePackScreen
│       ├── AlivePackScreen.java    # Main screen: HUD, drawers, toasts, sim controls
│       ├── SimState.java           # Game state (cash, rep, chaos, morale, time)
│       └── ToastManager.java       # Animated toast notifications
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

### Keyboard Shortcuts

| Key      | Action            |
|----------|-------------------|
| `Space`  | Pause / Resume    |
| `1`      | Speed x1          |
| `2`      | Speed x2          |
| `3`      | Speed x4          |

## Resources

- [libGDX Wiki](https://libgdx.com/wiki/)
- [libGDX API Documentation](https://libgdx.com/dev/api/)
- [libGDX Community](https://discord.gg/6pgDK9F)
