package ru.itl.lab.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ru.itl.lab.LabyTankGame;
import ru.itl.lab.utils.Constants;


public class NetScreen implements Screen {

    private final LabyTankGame game;
    private Skin skin;
    private ServerSocket serverSocket;

    boolean PRESSED = false;

    public NetScreen(LabyTankGame game) {
        this.game = game;
    }

    private Stage stage;
    private StretchViewport viewport;
    private OrthographicCamera camera;
    private TextField textField;
    private Integer port;
    private Screen nextScreen;
    private Texture back;

    @Override
    public void show() {

        back = new Texture("back.jpg");

        port = MathUtils.random(1025, 60000);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Port is already occupied");
            e.printStackTrace();
        }
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Constants.PORT = port;
                    Socket accept = serverSocket.accept();
                    serverSocket.close();
                    System.out.println("Accepted connection from: " + accept.getInetAddress());
                    nextScreen = new GameScreen(game, accept);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();

        camera = new OrthographicCamera();
        viewport = new StretchViewport(1280, 720, camera);
        stage = new Stage(viewport);
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        Label label = new Label("Your port is: " + port, skin);
        label.setColor(Color.BLACK);
        label.setPosition(90, 180);

        TextButton textButton = new TextButton("CONNECT", skin);
        textButton.setPosition(60, 60);
        textButton.setSize(600, 60);
        textButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try {
                            String host = text.substring(0, text.indexOf(":"));
                            int port = Integer.parseInt(text.substring(text.indexOf(":") + 1, text.length()));
                            System.out.println("Connecting to " + host + " " + port);
                            Socket socket = new Socket(host, port);
                            Constants.PORT = port;
                            Constants.HOST = host;

                            if (thread != null)
                                thread.interrupt();
                            nextScreen = new GameClientScreen(game, socket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void canceled() {

                    }
                }, "Enter the server's address", "", "host:port");
                return super.touchDown(event, x, y, pointer, button);
            }
        });


        Label labelHelp = new Label("To connect simply think with your head, silly", skin);
        labelHelp.setPosition(670, 180);
        labelHelp.setVisible(false);
        final TextButton textButton1 = new TextButton("How makes the connect", skin);
        textButton1.setPosition(670, 60);
        textButton1.setSize(600, 60);
        textButton1.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.net.openURI("https://drive.google.com/file/d/1kZQLtFdqRHiAWVLTbLi3kQ7fpheNjiX9/view");
                return super.touchDown(event, x, y, pointer, button);
            }
        });


        stage.addActor(label);
        stage.addActor(textButton);
        stage.addActor(textButton1);

        Gdx.input.setInputProcessor(this.stage);


    }

    @Override
    public void render(float delta) {

        stage.getViewport().apply();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 0);

        stage.getBatch().begin();
        stage.getBatch().draw(back, 0, 0);
        stage.getBatch().end();

        stage.draw();
        stage.act();
        if (nextScreen != null) {
            game.setScreen(nextScreen);
            nextScreen = null;
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
        back.dispose();
    }
}
