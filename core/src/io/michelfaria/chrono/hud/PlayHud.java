package io.michelfaria.chrono.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.michelfaria.chrono.Core;
import io.michelfaria.chrono.animation.ChronoOpenClosingAnimator;

public class PlayHud implements Disposable {

    public OrthographicCamera camera;
    public Viewport viewport;
    public Stage stage;

    private Group dialogBoxGroup;
    private Actor dialogBox;
    private ChronoOpenClosingAnimator dialogAnimator;

    public PlayHud() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Core.V_WIDTH, Core.V_HEIGHT);
        stage = new Stage(viewport, Core.batch);

        dialogBoxGroup = new Group();
        setDialogBoxType(0);
        dialogBox = new DialogBox();
        dialogBoxGroup.addActor(dialogBox);

        dialogAnimator = new ChronoOpenClosingAnimator(dialogBoxGroup, viewport);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public void draw() {
        stage.draw();
        dialogAnimator.draw();
    }

    public void update() {
        stage.act();

        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            dialogAnimator.open();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.O)) {
            dialogAnimator.close();
        }
    }

    /**
     * Sets the dialog box for the Hud.
     */
    public void setDialogBoxType(int type) {
        if (type < 0) {
            throw new IllegalArgumentException("type must be >= 0");
        }
        MenuBoxes.setUiType(type);
    }
}
