package ru.itl.lab.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.TimeUtils;

import java.sql.Time;
import java.util.HashSet;
import java.util.Set;

import ru.itl.lab.utils.Constants;

import static ru.itl.lab.utils.Constants.DOWN;
import static ru.itl.lab.utils.Constants.MAX_BULLETS;
import static ru.itl.lab.utils.Constants.ROTATE_LEFT;
import static ru.itl.lab.utils.Constants.ROTATE_RIGHT;
import static ru.itl.lab.utils.Constants.UP;

/**
 * Created by Булат on 17.03.2018.
 */

public class Tank extends Actor {

    private final World world;
    private boolean isPlayer;
    public boolean shouldBeDestroyed = false;
    Texture tankTexture;
    Sprite tankSprite;
    public float alpha = 0;

    Camera camera;
    Vector2 tankPosition;

    private Body tankBody;
    public Fixture tankFixture;

    private Runnable onDestroyRunnable;
    private Set<Bullet> bullets;
    private long lastShoot;

    public Tank(World world, Camera camera, float pX, float pY, boolean isPlayer) {
        this.camera = camera;
        this.world = world;
        this.isPlayer = isPlayer;
        this.bullets = new HashSet<Bullet>();

        tankPosition = new Vector2(pX, pY);
        createCircle(this.world, tankPosition.x, tankPosition.y);

        if (isPlayer)
            tankTexture = new Texture("tank_blue.png");
        else
            tankTexture = new Texture("tank_red.png");

        tankSprite = new Sprite(tankTexture);
        tankSprite.setPosition(tankPosition.x, tankPosition.y);
    }

    public Tank(World world, Camera camera, float pX, float pY) {
        this(world, camera, pX, pY, true);
    }

    public boolean canShoot() {
        return TimeUtils.millis() - lastShoot >= 500 && bullets.size() < MAX_BULLETS;
    }

    public void addBullet(Bullet bullet) {
        lastShoot = TimeUtils.millis();
        bullets.add(bullet);
    }

    public void removeBullet(Bullet bullet) {
        bullets.remove(bullet);
    }

    @Override
    public void act(float delta) {

        tankPosition.x = getTankBody().getPosition().x;
        tankPosition.y = getTankBody().getPosition().y;

        if (UP && isPlayer) {
            getTankBody().setLinearVelocity(getTankBody().getLinearVelocity().x -= Constants.TANK_SPEED * Math.sin(alpha * Math.PI / 180.0f) * delta,
                    getTankBody().getLinearVelocity().y += Constants.TANK_SPEED * Math.cos(alpha * Math.PI / 180.0f) * delta);
        }

        if (DOWN && isPlayer) {
            getTankBody().setLinearVelocity(getTankBody().getLinearVelocity().x += Constants.TANK_SPEED * Math.sin(alpha * Math.PI / 180.0f) * delta,
                    getTankBody().getLinearVelocity().y -= Constants.TANK_SPEED * Math.cos(alpha * Math.PI / 180.0f) * delta);
        }

        if (ROTATE_LEFT && isPlayer) {
            alpha += 3;
            if (alpha == 360) alpha = 0;
        }

        if (ROTATE_RIGHT && isPlayer) {
            alpha -= 3;
            if (alpha == 360) alpha = 0;
        }

        if ((!UP && !DOWN && !ROTATE_LEFT && !ROTATE_RIGHT) && isPlayer) {
            getTankBody().setLinearVelocity(0, 0);
        }

        setX(tankPosition.x);
        setY(tankPosition.y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setProjectionMatrix(camera.combined);
        batch.draw(tankSprite, tankPosition.x - (tankSprite.getWidth() / 2),
                tankPosition.y - (tankSprite.getHeight() / 2),
                tankSprite.getWidth() / 2,
                tankSprite.getHeight() / 2,
                tankSprite.getWidth(),
                tankSprite.getHeight(),
                1, 1, alpha);
    }

    private void createCircle(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(x, y);
        tankBody = world.createBody(bodyDef);
        getTankBody().setUserData(this);
        CircleShape shape = new CircleShape();
        shape.setRadius(20.0f);

        tankFixture = getTankBody().createFixture(shape, 1.0f);
        tankFixture.setFriction(0);
        shape.dispose();
    }

    public void dispose() {
        tankTexture.dispose();
        if (onDestroyRunnable != null)
            onDestroyRunnable.run();
        ((Tank) getTankBody().getUserData()).shouldBeDestroyed = true;
    }

    public void setOnDestroy(Runnable onDestroyRunnable) {
        this.onDestroyRunnable = onDestroyRunnable;
    }

    public Body getTankBody() {
        return tankBody;
    }
}
