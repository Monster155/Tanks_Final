package ru.itl.lab;

import com.badlogic.gdx.Game;
import ru.itl.lab.screens.GameScreen;
import ru.itl.lab.screens.NetScreen;

public class LabyTankGame extends Game {

	private LabyTankGame game;

	@Override
	public void create() {
		game = this;
		setScreen(new NetScreen(game));
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
