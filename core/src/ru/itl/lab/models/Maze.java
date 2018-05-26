package ru.itl.lab.models;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.itl.lab.utils.Constants;

import static ru.itl.lab.utils.Constants.JOYSTICK_SIZE_HEIGHT;
import static ru.itl.lab.utils.Constants.JOYSTICK_SIZE_WIDTH;

/**
 * Created by Булат on 17.03.2018.
 */

public class Maze extends Actor {

    Camera camera;
    Texture wall;
    Sprite wallSprite;

    private boolean goesUp[][] = new boolean[Constants.MAZE_WIDTH][Constants.MAZE_HEIGHT];
    private boolean goesDown[][] = new boolean[Constants.MAZE_WIDTH][Constants.MAZE_HEIGHT];
    private boolean goesLeft[][] = new boolean[Constants.MAZE_WIDTH][Constants.MAZE_HEIGHT];
    private boolean goesRight[][] = new boolean[Constants.MAZE_WIDTH][Constants.MAZE_HEIGHT];
    private boolean visited[][] = new boolean[Constants.MAZE_WIDTH][Constants.MAZE_HEIGHT];
    public ArrayList<Rectangle> walls = new ArrayList<Rectangle>();
    public static final float WALL_SIZE = 0.03f;
    private float width = 1;
    private float height = 1;

    Body mazeBody;
    public Fixture mazeFixture;

    public Maze(World world, Camera camera) {
        this.camera = camera;
        generate();

        wall = new Texture("wall.png");
        wallSprite = new Sprite(wall);
        wallSprite.setSize(wallSprite.getWidth() * JOYSTICK_SIZE_WIDTH,
                wallSprite.getHeight() * JOYSTICK_SIZE_HEIGHT);

        if (world != null)
            for (Rectangle rect :
                    walls) {
                createBox(world, (rect.x + rect.width / 2) * 80, (rect.y + rect.height / 2) * 80,
                        rect.width * 80, rect.height * 80);
            }
    }

    public void updateWorld(World world){
        for (Rectangle rect :
                walls) {
            createBox(world, (rect.x + rect.width / 2) * 80, (rect.y + rect.height / 2) * 80,
                    rect.width * 80, rect.height * 80);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setProjectionMatrix(camera.combined);
        for (Rectangle rect :
                walls) {
            batch.draw(wallSprite, rect.x * 80, rect.y * 80,
                    rect.width * 80, rect.height * 80);
        }
    }

    private void dfs(int x, int y) {
        visited[x][y] = true;

        List<Integer> integers = Arrays.asList(0, 1, 2, 3);
        Collections.shuffle(integers);
        for (Integer i :
                integers)
            switch (i) {

                case 0:
                    if (x > 0 && !visited[x - 1][y]) {
                        goesLeft[x][y] = true;
                        goesRight[x - 1][y] = true;
                        dfs(x - 1, y);
                    }
                    break;
                case 1:
                    if (x < Constants.MAZE_WIDTH - 1 && !visited[x + 1][y]) {
                        goesLeft[x + 1][y] = true;
                        goesRight[x][y] = true;
                        dfs(x + 1, y);
                    }
                    break;
                case 2:
                    if (y > 0 && !visited[x][y - 1]) {
                        goesDown[x][y] = true;
                        goesUp[x][y - 1] = true;
                        dfs(x, y - 1);
                    }
                    break;
                case 3:
                    if (y < Constants.MAZE_HEIGHT - 1 && !visited[x][y + 1]) {
                        goesUp[x][y] = true;
                        goesDown[x][y + 1] = true;
                        dfs(x, y + 1);
                    }
                    break;

            }
    }

    public void generate() {
        dfs(0, 0);
        for (int x = 0; x < Constants.MAZE_WIDTH; x++) {
            for (int y = 0; y < Constants.MAZE_HEIGHT; y++) {
                if (!goesUp[x][y])
                    walls.add(new Rectangle(x, y - WALL_SIZE / 2 + height, width, WALL_SIZE));
                if (!goesDown[x][y])
                    walls.add(new Rectangle(x, y - WALL_SIZE / 2, width, WALL_SIZE));
                if (!goesRight[x][y])
                    walls.add(new Rectangle(x + width - WALL_SIZE / 2, y, WALL_SIZE, height));
                if (!goesLeft[x][y])
                    walls.add(new Rectangle(x - WALL_SIZE / 2, y, WALL_SIZE, height));
            }
        }
    }

    private void createBox(World world, float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        bodyDef.position.set(x, y);
        mazeBody = world.createBody(bodyDef);
        mazeBody.setAwake(false);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);

        mazeFixture = mazeBody.createFixture(shape, 1.0f);
        mazeFixture.setRestitution(0);

        shape.dispose();
    }

    public void dispose() {
        wall.dispose();
    }

}
