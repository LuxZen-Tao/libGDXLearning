# libGDX Learning

A base libGDX template project for learning libGDX from the ground up.

## Project Structure

```
libGDXLearning/
├── core/                   # Core game code (platform-independent)
│   └── src/main/java/
│       └── com/libgdxlearning/
│           └── MyGdxGame.java
├── desktop/                # Desktop launcher
│   └── src/main/java/
│       └── com/libgdxlearning/
│           └── DesktopLauncher.java
├── build.gradle            # Main build configuration
└── settings.gradle         # Project settings
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

This will launch a window displaying "Welcome to libGDX!" with the current FPS.

## What's Included

- **MyGdxGame.java**: The main game class that extends `ApplicationAdapter`
  - `create()`: Initialize resources (runs once)
  - `render()`: Main game loop (runs every frame)
  - `dispose()`: Clean up resources

- **DesktopLauncher.java**: Desktop entry point
  - Configures window size (800x600)
  - Sets target FPS (60)
  - Creates the application

## Next Steps

Start modifying `MyGdxGame.java` to build your game:
- Add sprites and textures
- Implement game logic
- Add input handling
- Create game screens
- And more!

## Resources

- [libGDX Wiki](https://libgdx.com/wiki/)
- [libGDX API Documentation](https://libgdx.com/dev/api/)
- [libGDX Community](https://discord.gg/6pgDK9F)
