package game;

import java.awt.*;
import java.awt.event.KeyEvent;

import engine.*;

public class TestRectangleGuy extends GameObject {

	private int width = 25, height = 35;
	private double x, y;
	private double moveSpeed = 250; // pixels per second
	
	public void onStart(){
		x = Game.WIDTH / 2;
		y = Game.HEIGHT / 2;
	}
	
	public void update(double dt){
		if(Input.isDown(KeyEvent.VK_LEFT))
			x -= moveSpeed * dt;
		if(Input.isDown(KeyEvent.VK_RIGHT))
			x += moveSpeed * dt;
		if(Input.isDown(KeyEvent.VK_UP))
			y -= moveSpeed * dt;
		if(Input.isDown(KeyEvent.VK_DOWN))
			y += moveSpeed * dt;
		
		x = Utils.clamp(x, width/2, Game.WIDTH -width/2);
		y = Utils.clamp(y, height/2, Game.HEIGHT - height/2);
	}
	
	public void draw(Graphics2D g){
		g.setColor(Color.white);
		Utils.fillRect(g, x, y, width, height, 0, 1);
	}

}
