package ru.itl.lab.utils;

import com.badlogic.gdx.Gdx;

/**
 * Created by Булат on 17.03.2018.
 */

public final class Constants {

    public static final float TANK_SPEED = 10000.0f;
    public static final float WORLD_WIDTH = Gdx.graphics.getWidth();
    public static final float WORLD_HEIGHT = Gdx.graphics.getHeight();

    public static final int MAZE_WIDTH = 16;
    public static final int MAZE_HEIGHT = 9;

    public static final float JOYSTICK_SIZE_WIDTH = WORLD_WIDTH / 1280;
    public static final float JOYSTICK_SIZE_HEIGHT = WORLD_HEIGHT / 720;

    public static boolean UP, DOWN, ROTATE_LEFT, ROTATE_RIGHT, SHOOT = false, CHECK = true;
    public static final int MAX_BULLETS = 5;
    public static int SCORE = 0;

    public static final String WALLS_PACKET_START = "WALLS:";
    public static final String TANK_PACKET = "TANK";
    public static final String BULLETS_PACKET_START = "BULLETS";

    public static final String SHOOT_PACKET = "SHOOT";
    public static final String PACKET_END = "PACKET_END";
    public static final String GAME_OVER_PACKET = "GAME_OVER";
    public static int ALIVE_THREADS = 0;
    public static String HOST;
    public static int PORT;

}
