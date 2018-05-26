package ru.itl.lab.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
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

public class GameClientScreen implements Screen {

    private LabyTankGame game;
    private Socket serverSocket;
    private World world;
    private Box2DDebugRenderer renderer;
    private Tank enemyTank;
    private Tank tank;
    private ArrayList<Bullet> bullets;
    private InputStream inputStream;
    private OutputStream outputStream;
    Joystick joystick;
    private HashMap<Fixture, Object> fixtureObjectMap;
    private boolean isAlive = true;
    private Thread thread;

    public GameClientScreen(LabyTankGame game, Socket socket) {
        this.game = game;
        this.serverSocket = socket;
    }

    Stage stage;
    StretchViewport viewport;
    OrthographicCamera camera;
    Maze maze;

    @Override
    public void show() {
        if (serverSocket.isClosed()) {
            game.setScreen(new NetScreen(game));
            isAlive = false;
            return;
        }
        fixtureObjectMap = new HashMap<Fixture, Object>();

        Gdx.input.setInputProcessor(null);
        world = new World(new Vector2(0, 0), false);
        renderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(1280, 720, camera);
        stage = new Stage(viewport);

        joystick = new Joystick(stage.getCamera());
        stage.addActor(joystick);

        maze = new Maze(null, stage.getCamera());
        stage.addActor(maze);

        //receive walls data
        maze.walls.clear();
        try {
            inputStream = serverSocket.getInputStream();
            outputStream = serverSocket.getOutputStream();
            char cur;
            StringBuilder line;
            while (true) {

                line = new StringBuilder();
                cur = (char) (inputStream.read());
                while (cur != '\n') {
                    line.append(cur);
                    cur = (char) (inputStream.read());
                }
                System.out.println(line);
                if (line.toString().startsWith(WALLS_PACKET_START))
                    break;

            }
            System.out.println(line);
            Integer total = Integer.valueOf(line.toString().substring(WALLS_PACKET_START.length(), line.toString().length()));
            for (int i = 0; i < total; i++) {
                cur = (char) (inputStream.read());
                line = new StringBuilder();
                while (cur != '\n') {
                    line.append(cur);
                    cur = (char) (inputStream.read());
                }
                String[] splits = line.toString().split("\\|");
                Float x = Float.valueOf(splits[0]);
                Float y = Float.valueOf(splits[1]);
                Float width = Float.valueOf(splits[2]);
                Float height = Float.valueOf(splits[3]);
                maze.walls.add(new Rectangle(x, y, width, height));
            }
            maze.updateWorld(world);
        } catch (IOException e) {
            e.printStackTrace();
        }
        enemyTank = new Tank(world, stage.getCamera(), 300, 300, false);
        tank = new Tank(world, stage.getCamera(), 600, 300);
        stage.addActor(enemyTank);
        stage.addActor(tank);
        bullets = new ArrayList<Bullet>();
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

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
        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                ALIVE_THREADS++;
                while (isAlive) {

                    try {
                        if (serverSocket.isClosed()) {

                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    game.setScreen(new NetScreen(game));
                                }
                            });
                            isAlive = false;
                            break;
                        }
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
                            //System.out.println(line);
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
                                case GAME_OVER_PACKET:
                                    isAlive = false;
                                    Gdx.app.postRunnable(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (Boolean.valueOf(splits[1]))
                                                SCORE++;
                                            game.setScreen(new GameOverScreen(game, serverSocket, true));
                                        }
                                    });
                                    break kek;
                                case BULLETS_PACKET_START:
                                    fixtureObjectMap.clear();
                                    Gdx.app.postRunnable(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (Bullet bullet :
                                                    bullets) {
                                                world.destroyBody(bullet.bulletBody);
                                            }
                                            bullets.clear();
                                        }
                                    });

                                    int total = Integer.parseInt(splits[1]);
                                    for (int i = 0; i < total; i++) {


                                        StringBuilder line1 = new StringBuilder();
                                        char cur1 = (char) (inputStream.read());
                                        while (cur1 != '\n') {
                                            line1.append(cur1);
                                            cur1 = (char) (inputStream.read());
                                        }
                                        final String[] bulletData = line1.toString().split("\\|");
                                        Gdx.app.postRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                final Bullet bullet = new Bullet(world, enemyTank, 0);
                                                bullet.getBulletBody().setLinearVelocity(Float.parseFloat(bulletData[2]), Float.parseFloat(bulletData[3]));
                                                bullet.getBulletBody().setTransform(Float.parseFloat(bulletData[0]), Float.parseFloat(bulletData[1]), 0);
                                                bullets.add(bullet);
                                                fixtureObjectMap.put(bullet.bulletFixture, bullet);
                                            }
                                        });
                                    }
                                    break;
                            }
                            if (line.toString().startsWith(PACKET_END))
                                break;
                        }

                        outputStream.write((String.format("%s|%s|%s|%s|%s|%s\n", TANK_PACKET, tank.getTankBody().getPosition().x, tank.getTankBody().getPosition().y, tank.getTankBody().getLinearVelocity().x, tank.getTankBody().getLinearVelocity().y, tank.alpha)).getBytes());
                        outputStream.write((String.format("%s|%s\n", SHOOT_PACKET, SHOOT)).getBytes());
                        SHOOT = false;
                        outputStream.write(String.format("%s\n", PACKET_END).getBytes());
                        Thread.sleep(10);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                game.setScreen(new NetScreen(game));
                            }
                        });
                        isAlive = false;
                    }

                }

                ALIVE_THREADS--;
            }
        });
        thread.start();
    }

    @Override
    public void render(float delta) {
        //now do stuff
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(1, 1, 1, 0);

        stage.getViewport().apply();
        world.step(delta, 6, 2);
        stage.act(delta);
        stage.draw();


        for (Bullet bullet :
                bullets) {
            bullet.draw(stage, delta);
        }
    }

    @Override
    public void resize(int width, int height) {

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
        enemyTank.dispose();
        tank.dispose();
        maze.dispose();
        stage.dispose();
        world.dispose();
        renderer.dispose();
        isAlive = false;
    }
}
