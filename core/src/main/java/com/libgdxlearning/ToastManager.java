package com.libgdxlearning;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;

public class ToastManager {

    private final Group root;   // where to attach toasts (usually Stage root)
    private final Skin skin;

    // Layout tuning
    private float margin = 14f;
    private float spacing = 8f;

    public ToastManager(Group root, Skin skin) {
        this.root = root;
        this.skin = skin;
    }

    public void show(String message) {
        // Create toast label
        final Label toast = new Label(message, skin);
        toast.setAlignment(Align.left);

        // Start invisible/off position
        toast.getColor().a = 0f;

        // Position at top-right; stack downward as more toasts appear
        float stageW = root.getStage().getViewport().getWorldWidth();
        float stageH = root.getStage().getViewport().getWorldHeight();

        // Force layout to get size
        toast.pack();

        float x = stageW - margin - toast.getWidth();
        float y = stageH - margin - toast.getHeight();

        // Push down existing toasts
        for (var actor : root.getChildren()) {
            if (actor.getName() != null && actor.getName().startsWith("toast_")) {
                actor.addAction(Actions.moveBy(0, -(toast.getHeight() + spacing), 0.12f));
            }
        }

        toast.setPosition(x, y);
        toast.setName("toast_" + System.nanoTime());

        root.addActor(toast);

        // Animate: slide in a bit + fade in, then wait, then fade out + slide up, then remove
        toast.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.moveBy(0, 10f, 0.18f),
                        Actions.fadeIn(0.18f)
                ),
                Actions.delay(1.4f),
                Actions.parallel(
                        Actions.moveBy(0, 10f, 0.22f),
                        Actions.fadeOut(0.22f)
                ),
                Actions.removeActor()
        ));
    }
}
