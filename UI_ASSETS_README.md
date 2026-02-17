# UI Skin Assets

This project includes the standard libGDX UI skin assets for creating user interfaces.

## Included Files

The following UI skin files are located in `desktop/src/main/resources/assets/`:

- **uiskin.json** - The skin definition file that defines all UI styles and components
- **uiskin.atlas** - Texture atlas definition file that maps UI elements to texture regions
- **uiskin.png** - Texture atlas image containing all UI element graphics
- **default.fnt** - Bitmap font file for text rendering
- **default.png** - Bitmap font texture image

## Usage

To use the UI skin in your screens:

```java
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

// In your Screen's show() method:
Stage stage = new Stage();
Skin skin = new Skin(Gdx.files.internal("assets/uiskin.json"));

// Create UI elements
Label label = new Label("Hello World", skin);
TextButton button = new TextButton("Click Me", skin);

// Add to stage
stage.addActor(label);
stage.addActor(button);

// Don't forget to dispose
@Override
public void dispose() {
    stage.dispose();
    skin.dispose();
}
```

## Example

See `UISkinTestScreen.java` for a complete example of loading and using the UI skin.

## Source

These assets are the standard libGDX UI skin files from the official libGDX repository:
https://github.com/libgdx/libgdx/tree/master/tests/gdx-tests-android/assets/data
