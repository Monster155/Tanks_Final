package ru.itl.lab.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import static ru.itl.lab.utils.Constants.DOWN;
import static ru.itl.lab.utils.Constants.ROTATE_LEFT;
import static ru.itl.lab.utils.Constants.ROTATE_RIGHT;
import static ru.itl.lab.utils.Constants.SCORE;
import static ru.itl.lab.utils.Constants.SHOOT;
import static ru.itl.lab.utils.Constants.UP;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class Joystick extends Actor {

    Camera camera;
    Vector3 touchPos;


    private SpriteBatch batch;
    private BitmapFont Score1;
    private BitmapFont Score2;

    Stage stage;
    public static BitmapFont font, shadow;

    private Texture up, down, left, right, shoot;
    private Sprite upSprite, downSprite, leftSprite, rightSprite, shootSprite;

    public Joystick(Camera camera) {

        batch = new SpriteBatch();

        Score1 = new BitmapFont();
        Score1.setColor(Color.BLACK);
        Score2 = new BitmapFont();
        Score2.setColor(Color.BLACK);

        this.camera = camera;
        batch = new SpriteBatch();
        up = new Texture("up.png");
        down = new Texture("down.png");
        right = new Texture("right.png");
        left = new Texture("left.png");
        shoot = new Texture("shoot.png");

        rightSprite = new Sprite(right);
        leftSprite = new Sprite(left);
        shootSprite = new Sprite(shoot);
        upSprite = new Sprite(up);
        downSprite = new Sprite(down);

        touchPos = new Vector3();
    }

    @Override
    public void act(float delta) {
        UP = false;
        DOWN = false;
        ROTATE_LEFT = false;
        ROTATE_RIGHT = false;

        for (int i = 0; i < 10; i++) {

            if (Gdx.input.isTouched(i)) {

                touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                camera.unproject(touchPos);

                if (touchPos.x > 20 && touchPos.x < 20 + upSprite.getWidth() && touchPos.y < upSprite.getHeight() + upSprite.getHeight()
                        && touchPos.y > upSprite.getHeight()) {
                    UP = true;
                } else {
                    UP = false;
                }

                if (touchPos.x > 20 && touchPos.x < 20 + downSprite.getWidth() && touchPos.y < downSprite.getHeight() && touchPos.y > 0) {
                    DOWN = true;
                } else {
                    DOWN = false;
                }

                if (touchPos.x > 956 && touchPos.x < 1064 && touchPos.y < leftSprite.getHeight() && touchPos.y > 0) {
                    ROTATE_LEFT = true;
                } else {
                    ROTATE_LEFT = false;
                }

                if (touchPos.x > 1172 && touchPos.x < 1280 && touchPos.y < rightSprite.getHeight() && touchPos.y > 0) {
                    ROTATE_RIGHT = true;
                } else {
                    ROTATE_RIGHT = false;
                }
            }

            if (Gdx.input.isTouched(i)) {
                touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                camera.unproject(touchPos);

                if (touchPos.x > 1064 && touchPos.x < 1172 &&
                        touchPos.y < shootSprite.getHeight() + 72 && touchPos.y > shootSprite.getHeight()) {
                    SHOOT = true;
                }
            }

        }

    }



    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setProjectionMatrix(camera.combined);
        batch.draw(upSprite, 20, upSprite.getHeight());
        batch.draw(downSprite, 20, 0);
        batch.draw(rightSprite, 1172, 0);
        batch.draw(shootSprite, 1064, 72);
        batch.draw(leftSprite, 956, 0);

        Score1.getData().setScale(3);
        Score2.getData().setScale(3);
        Score1.draw(batch, "SCORE: ", 20, 700);
        Score2.draw(batch, SCORE + "", 200, 700);

    }

    public void dispose() {
        up.dispose();
        down.dispose();
        right.dispose();
        left.dispose();
        shoot.dispose();
        shadow.dispose();
        font.dispose();
    }
}
