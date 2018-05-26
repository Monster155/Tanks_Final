package ru.itl.lab.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.net.ServerSocket;
import java.net.Socket;

import ru.itl.lab.LabyTankGame;
import ru.itl.lab.utils.Constants;

import static ru.itl.lab.utils.Constants.PORT;
import static ru.itl.lab.utils.Constants.SCORE;


public class GameOverScreen implements Screen {

    public static BitmapFont font, shadow;
    SpriteBatch batch;
    LabyTankGame game;
    private Socket socket;
    private boolean isClient;
    OrthographicCamera camera;
    StretchViewport viewport;
    Stage stage;
    Vector3 touchPos;
    private boolean oneFrame = false;

    public GameOverScreen(LabyTankGame game, Socket socket, boolean isClient) {
        this.game = game;
        this.socket = socket;
        this.isClient = isClient;
    }

    @Override
    public void show() {
        Constants.SHOOT = false;
        touchPos = new Vector3();
        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        font = new BitmapFont(Gdx.files.internal("data/text.fnt"));
        shadow = new BitmapFont(Gdx.files.internal("data/shadow.fnt"));

        viewport = new StretchViewport(1280, 720, camera);
        stage = new Stage(viewport);
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getViewport().apply();

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        shadow.draw(batch, "Score", 540, 600);
        font.draw(batch, "Score", 540, 600);

        shadow.draw(batch, SCORE + "", 640, 360);
        font.draw(batch, SCORE + "", 640, 360);

        shadow.draw(batch, "Tap to continue...", 340, 125);
        font.draw(batch, "Tap to continue...", 340, 125);
        batch.end();

        if (oneFrame)
            try {
                if (Gdx.input.justTouched()) {
                    if (isClient) {

                        while (true) {
                            try {
                                Socket socket = new Socket(Constants.HOST, PORT);
                                game.setScreen(new GameClientScreen(game, socket));
                                Thread.sleep(50);
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    } else {
                        ServerSocket serverSocket = new ServerSocket(Constants.PORT);
                        Socket accept = serverSocket.accept();
                        serverSocket.close();
                        game.setScreen(new GameScreen(game, accept));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        oneFrame = true;
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
        font.dispose();
        shadow.dispose();
        stage.dispose();
    }
}
