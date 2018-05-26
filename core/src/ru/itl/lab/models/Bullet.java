package ru.itl.lab.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;

import static ru.itl.lab.utils.Constants.SHOOT;
import static ru.itl.lab.utils.Constants.TANK_SPEED;

/**
 * Created by Булат on 24.03.2018.
 */

public class Bullet {

    public Body bulletBody;
    private World world;
    Tank tank;
    ShapeRenderer renderer;
    public Fixture bulletFixture;
    public float alpha = 0;
    public long spawnTime;
    public boolean shouldBeDestroyed = false;

    public Bullet(World world, Tank tank, long spawnTime) {
        this.world = world;
        this.tank = tank;
        this.spawnTime = spawnTime;
        renderer = new ShapeRenderer();
        alpha = tank.alpha;

        createCircle(world, tank.getTankBody().getPosition().x - 30 * (float) Math.cos((tank.alpha - 90) * Math.PI / 180.0f),
                tank.getTankBody().getPosition().y - 30 * (float) Math.sin((tank.alpha - 90) * Math.PI / 180.0f));


        bulletBody.setLinearVelocity( -(float) Math.cos((alpha - 90) * Math.PI / 180.0f) * 2 * TANK_SPEED ,
                 -(float) Math.sin((alpha - 90) * Math.PI / 180.0f) * 2 * TANK_SPEED );
        tank.addBullet(this);
    }


    public void draw(Stage stage, float delta) {

        renderer.setProjectionMatrix(stage.getCamera().combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.BLACK);
        renderer.circle(bulletBody.getPosition().x,
                bulletBody.getPosition().y, 5);
        renderer.end();
    }

    private void createCircle(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(x, y);
        bulletBody = world.createBody(bodyDef);
        bulletBody.setUserData(this);
        bulletBody.setBullet(true);

        CircleShape shape = new CircleShape();
        shape.setRadius(5.0f);

        bulletFixture = bulletBody.createFixture(shape, 1.0f);
        bulletFixture.setRestitution(1.0f);
        bulletFixture.setFriction(0);

        shape.dispose();
    }

    public void dispose() {
        tank.removeBullet(this);
        renderer.dispose();
        ((Bullet) bulletBody.getUserData()).shouldBeDestroyed = true;
    }

    public Body getBulletBody() {
        return bulletBody;
    }
}

