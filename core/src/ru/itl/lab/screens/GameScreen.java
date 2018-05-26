package ru.itl.lab.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import ru.itl.lab.LabyTankGame;
import ru.itl.lab.models.Bullet;
import ru.itl.lab.models.Joystick;
import ru.itl.lab.models.Maze;
import ru.itl.lab.models.Tank;

import static ru.itl.lab.utils.Constants.ALIVE_THREADS;
import static ru.itl.lab.utils.Constants.BULLETS_PACKET_START;
import static ru.itl.lab.utils.Constants.GAME_OVER_PACKET;
import static ru.itl.lab.utils.Constants.PACKET_END;
import static ru.itl.lab.utils.Constants.SCORE;
import static ru.itl.lab.utils.Constants.SHOOT;
import static ru.itl.lab.utils.Constants.SHOOT_PACKET;
import static ru.itl.lab.utils.Constants.TANK_PACKET;
import static ru.itl.lab.utils.Constants.WALLS_PACKET_START;


public class GameScreen implements Screen {

    private LabyTankGame game;
    private Socket clientSocket;

    private HashMap<Fixture, Object> fixtureObjectMap;

    Stage stage;
    StretchViewport viewport;
    OrthographicCamera camera;
    final private ArrayList<Bullet> bullets;

    Tank tank;
    Joystick joystick;
    Maze maze;

    World world;
    Box2DDebugRenderer renderer;

    private OutputStream outputStream;
    private InputStream inputStream;
    private Tank enemyTank;
    private boolean isAlive = true;
    private Thread thread;

    public GameScreen(LabyTankGame game, Socket socket) {
        this.game = game;
        this.clientSocket = socket;
        bullets = new ArrayList<>();
    }

    @Override
    public void show() {
        if (clientSocket.isClosed()) {
            game.setScreen(new NetScreen(game));
            isAlive = false;
            return;
        }
        Gdx.input.setInputProcessor(null);
        fixtureObjectMap = new HashMap<Fixture, Object>();

        world = new World(new Vector2(0, 0), false);
        renderer = new Box2DDebugRenderer();

        camera = new OrthographicCamera();
        viewport = new StretchViewport(1280, 720, camera);
        stage = new Stage(viewport);

        joystick = new Joystick(stage.getCamera());
        tank = new Tank(world, stage.getCamera(), 300, 300);
        enemyTank = new Tank(world, stage.getCamera(), 600, 300, false);
        fixtureObjectMap.put(tank.tankFixture, tank);
        fixtureObjectMap.put(enemyTank.tankFixture, enemyTank);


        maze = new Maze(world, stage.getCamera());

        stage.addActor(joystick);
        stage.addActor(tank);
        stage.addActor(maze);
        stage.addActor(enemyTank);


        tank.setOnDestroy(new Runnable() {
            @Override
            public void run() {
                if (!isAlive)
                    return;
                isAlive = false;
                try {
                    while (ALIVE_THREADS != 0) Thread.sleep(10);
                    outputStream.write((String.format("%s|%s\n", GAME_OVER_PACKET, true)).getBytes());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        game.setScreen(new GameOverScreen(game, clientSocket, false));
                    }
                });
            }
        });
        enemyTank.setOnDestroy(new Runnable() {
            @Override
            public void run() {
                if (!isAlive)
                    return;
                isAlive = false;
                SCORE++;
                try {
                    while (ALIVE_THREADS != 0) Thread.sleep(10);
                    outputStream.write((String.format("%s|%s\n", GAME_OVER_PACKET, false)).getBytes());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        game.setScreen(new GameOverScreen(game, clientSocket, false));
                    }
                });
            }
        });

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                if (!fixtureObjectMap.containsKey(contact.getFixtureA()))
                    return;
                if (!fixtureObjectMap.containsKey(contact.getFixtureB()))
                    return;
                Object tank = fixtureObjectMap.get(contact.getFixtureA()), bullet = fixtureObjectMap.get(contact.getFixtureB());

                if (tank instanceof Bullet && bullet instanceof Bullet) {
                    contact.setEnabled(false);
                    return;
                }
                if (tank instanceof Tank && bullet instanceof Tank) {
                    contact.setEnabled(false);
                    return;
                }

                if (tank instanceof Bullet && bullet instanceof Tank) {
                    Object temp = tank;
                    tank = bullet;
                    bullet = temp;
                }
                if (!(tank instanceof Tank && bullet instanceof Bullet)) {
                    System.out.println("WTF");
                    return;
                }
                synchronized (bullets) {
                    bullets.remove(bullet);
                }
                ((Bullet) bullet).dispose();
                ((Tank) tank).dispose();

            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                if (!fixtureObjectMap.containsKey(contact.getFixtureA()))
                    return;
                if (!fixtureObjectMap.containsKey(contact.getFixtureB()))
                    return;
                Object a = fixtureObjectMap.get(contact.getFixtureA());
                Object b = fixtureObjectMap.get(contact.getFixtureB());

                if (a instanceof Bullet && b instanceof Bullet) {
                    contact.setEnabled(false);
                    return;
                }

                if (a instanceof Tank && b instanceof Tank) {
                    contact.setEnabled(false);
                    return;
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });


        //now send data to client
        try {
            outputStream = clientSocket.getOutputStream();
            inputStream = clientSocket.getInputStream();
            outputStream.write((WALLS_PACKET_START + maze.walls.size() + "\n").getBytes());
            for (Rectangle rectangle : maze.walls) {
                outputStream.write((rectangle.x + "|" + rectangle.y + "|" + rectangle.width + "|" + rectangle.height + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //now interact with client
                ALIVE_THREADS++;
                while (isAlive)
                    try {
                        if (clientSocket.isClosed()) {
                            game.setScreen(new NetScreen(game));
                            isAlive = false;
                            break;
                        }
                        outputStream.write((String.format("%s|%s|%s|%s|%s|%s\n", TANK_PACKET, tank.getTankBody().getPosition().x, tank.getTankBody().getPosition().y, tank.getTankBody().getLinearVelocity().x, tank.getTankBody().getLinearVelocity().y, tank.alpha)).getBytes());
                        synchronized (bullets) {
                            outputStream.write((String.format("%s|%s\n", BULLETS_PACKET_START, bullets.size())).getBytes());
                            for (Bullet bullet : bullets) {
                                outputStream.write((String.format("%s|%s|%s|%s\n", bullet.getBulletBody().getPosition().x, bullet.getBulletBody().getPosition().y, bullet.getBulletBody().getLinearVelocity().x, bullet.getBulletBody().getLinearVelocity().y)).getBytes());
                            }
                        }
                        outputStream.write(String.format("%s\n", PACKET_END).getBytes());

                        char cur;
                        StringBuilder line;
                        kek:
                        while (true) {
                            line = new StringBuilder();
                            cur = (char) (inputStream.read());
                            while (cur != '\n') {
                                line.append(cur);
                                cur = (char) (inputStream.read());
                            }

                            final String[] splits = line.toString().split("\\|");
                            switch (splits[0]) {
                                case PACKET_END:
                                    break kek;
                                case TANK_PACKET:
                                    Gdx.app.postRunnable(new Runnable() {
                                        @Override
                                        public void run() {
                                            enemyTank.getTankBody().setLinearVelocity(Float.parseFloat(splits[3]), Float.parseFloat(splits[4]));
                                            enemyTank.getTankBody().setTransform(Float.parseFloat(splits[1]), Float.parseFloat(splits[2]), 0);
                                            enemyTank.alpha = Float.parseFloat(splits[5]);
                                        }
                                    });

                                    break;
                                case SHOOT_PACKET:

                                    boolean enemyShoot = Boolean.parseBoolean((splits[1]));
                                    if (enemyShoot && enemyTank.canShoot()) {
                                        Gdx.app.postRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                synchronized (bullets) {
                                                    Bullet bullet = new Bullet(world, enemyTank, TimeUtils.millis());
                                                    bullets.add(bullet);
                                                    fixtureObjectMap.put(bullet.bulletFixture, bullet);
                                                }
                                            }
                                        });
                                    }
                                    break;
                            }
                            if (line.toString().startsWith(PACKET_END))
                                break;
                        }
                        Thread.sleep(10);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                game.setScreen(new NetScreen(game));
                            }
                        });
                        isAlive = false;
                    } catch (InterruptedException e) {
                        isAlive = false;
                        e.printStackTrace();
                    }
                ALIVE_THREADS--;
            }
        });
        thread.start();
    }

    @Override
    public void render(float delta) {

        Array<Body> worldBodies = new Array<Body>();
        world.getBodies(worldBodies);
        for (Body body :
                worldBodies) {
            Object userData = body.getUserData();
            if (userData instanceof Bullet && ((Bullet) userData).shouldBeDestroyed)
                world.destroyBody(body);
            if (userData instanceof Tank && ((Tank) userData).shouldBeDestroyed)
                world.destroyBody(body);

        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(1, 1, 1, 0);

        stage.getViewport().apply();
        world.step(delta, 6, 2);
        stage.act(delta);

        renderer.render(world, stage.getCamera().combined);
        stage.draw();

        if (SHOOT && tank.canShoot()) {
            synchronized (bullets) {
                Bullet bullet = new Bullet(world, tank, TimeUtils.millis());
                bullets.add(bullet);
                fixtureObjectMap.put(bullet.bulletFixture, bullet);
            }
        }
        SHOOT = false;


        ArrayList<Bullet> disposedBullets = new ArrayList<Bullet>();
        synchronized (bullets) {
            for (Bullet bullet :
                    bullets) {
                if (TimeUtils.millis() - bullet.spawnTime > 18000) {
                    bullet.dispose();
                    disposedBullets.add(bullet);
                }
            }


            for (Bullet bullet : disposedBullets) {
                bullets.remove(bullet);
                fixtureObjectMap.remove(bullet.bulletFixture);
            }
            for (Bullet bullet :
                    bullets) {
                bullet.draw(stage, delta);
            }
        }

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        joystick.dispose();
        tank.dispose();
        enemyTank.dispose();
        maze.dispose();
        stage.dispose();
        world.dispose();
        renderer.dispose();
        isAlive = false;
    }
}
