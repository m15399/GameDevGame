package testGames;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.sound.sampled.Clip;

import engine.*;

/**
 * Simple snake game - ran out of time and didn't do collision detection
 */
public class SnakeTest extends GameObject {

	public static void main(String[] args) {
		Application.launch();
		new SnakeTest();
	}

	
	static final int THICKNESS = 15;
	static final double MOVE_SPEED = 200;
	

	class TailSegment extends GameObject {
		double x1, x2, y1, y2; // start/end points
		double xMin, xMax, yMin, yMax; // bounding coords
		
		public TailSegment(double x1, double y1){
			setLine(x1, y1, x1, y1);
		}
		
		
		public void setEnd(double x2, double y2){
			setLine(x1, y1, x2, y2);
		}
		
		public void setLine(double x1, double y1, double x2, double y2){
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			
			updateBounds();
		}
		
		void updateBounds(){
			xMin = Math.min(x1, x2) - THICKNESS;
			xMax = Math.max(x1, x2) + THICKNESS;
			yMin = Math.min(y1, y2) - THICKNESS;
			yMax = Math.max(y1, y2) + THICKNESS;
		}
		
		// makes the tail follow the player instead of getting
		//   longer and longer 
		// returns how much was shrunk by (might not be full amt)
		public double shrink(double amt){
			
			double xLen = x2 - x1;
			double yLen = y2 - y1;
			double amtToShrink = 0;
			
			if(yLen == 0){
				double sign = xLen / Math.abs(xLen); // either -1 or 1
				amtToShrink = Math.min(amt, Math.abs(xLen));
				
				x1 += sign * amtToShrink;
				
			} else {
				double sign = yLen / Math.abs(yLen); // either -1 or 1
				amtToShrink = Math.min(amt, Math.abs(yLen));
				
				y1 += sign * amtToShrink;
			}
			
			updateBounds();

			return amtToShrink;
		}
		
		public void draw(Graphics2D g){
			// draw a rect using the bounds coords
			g.setColor(Color.gray);
			g.fillRect((int)xMin, (int)yMin, (int)(xMax-xMin), (int)(yMax-yMin));	
		}
		
	}
	
	class Player extends GameObject {
		
		static final double DISABLE_INPUT_TIME = THICKNESS * 2 / MOVE_SPEED;
		
		double x, y;
		double xv, yv;

		double tailLength, currLength;
		
		ArrayList<TailSegment> tailSegs;
		
		double disableInputTimeLeft;

		
		public Player(){
			x = Game.WIDTH/2;
			y = Game.HEIGHT/2;
			xv = MOVE_SPEED;
			yv = 0;
			
			currLength = 0;
			tailLength = 10;
			
			setDrawOrder(1);
			
			disableInputTimeLeft = 0;
			
			// create tail
			tailSegs = new ArrayList<TailSegment>();
			addNewSeg();

		}
		
		void addNewSeg(){
			// add a segment starting at our current location
			tailSegs.add(new TailSegment(x, y));
		}
		
		public void update(double dt){
			
			// move us and front of tail
			x += xv * dt;
			y += yv * dt;
			tailSegs.get(tailSegs.size()-1).setEnd(x, y);

			// shorten end of tail
			currLength += Math.max(Math.abs(xv), Math.abs(yv)) * dt;			
			while(currLength > tailLength){
				double amtToShrink = currLength-tailLength;
				double amtShrunk = tailSegs.get(0).shrink(amtToShrink);
				
				// if last segment was shrunk all the way to 0 length, destroy 
				if(amtShrunk < amtToShrink){
					tailSegs.get(0).destroy();
					tailSegs.remove(0);
				}
				
				currLength -= amtShrunk;
			}
			
			
			// process input
			disableInputTimeLeft -= dt;
			
			if(disableInputTimeLeft <= 0){
				
				if(Input.isDown(KeyEvent.VK_UP) && yv == 0){
					addNewSeg();
					xv = 0;
					yv = -MOVE_SPEED;
					disableInputTimeLeft = DISABLE_INPUT_TIME;
				} else if(Input.isDown(KeyEvent.VK_DOWN) && yv == 0){
					addNewSeg();
					xv = 0;
					yv = MOVE_SPEED;
					disableInputTimeLeft = DISABLE_INPUT_TIME;
				} else if(Input.isDown(KeyEvent.VK_LEFT) && xv == 0){
					addNewSeg();
					xv = -MOVE_SPEED;
					yv = 0;
					disableInputTimeLeft = DISABLE_INPUT_TIME;
				} else if(Input.isDown(KeyEvent.VK_RIGHT) && xv == 0){
					addNewSeg();
					xv = MOVE_SPEED;
					yv = 0;
					disableInputTimeLeft = DISABLE_INPUT_TIME;
				}
			}
			
		}
		
		public void draw(Graphics2D g){
			g.setColor(Color.cyan);
			g.fillRect((int)(x-THICKNESS), (int)(y-THICKNESS), THICKNESS*2, THICKNESS*2);
		}
		
	}
	
	class Pickup extends GameObject {
		
		double x, y;
		
		public Pickup(){
			relocate();
			setDrawOrder(.5);
		}
		
		public void relocate(){
			x = Utils.randomRange(THICKNESS, Game.WIDTH-THICKNESS);
			y = Utils.randomRange(THICKNESS, Game.HEIGHT-THICKNESS);
		}
		
		public void draw(Graphics2D g){
			g.setColor(Color.green);
			g.fillRect((int)(x-THICKNESS), (int)(y-THICKNESS), THICKNESS*2, THICKNESS*2);
		}
		
	}
	
	
	
	Player player;
	Pickup pickup;
	
	
	public void onStart(){
		player = new Player();
		pickup = new Pickup();
		
		// music
		Clip c = Resources.getSound("test.wav");
		c.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	public void update(double dt){
		// check player vs pickup collisions
		if(Utils.rectsCollide(
				player.x - THICKNESS, player.y - THICKNESS, THICKNESS*2, THICKNESS*2, 
				pickup.x - THICKNESS, pickup.y - THICKNESS, THICKNESS*2, THICKNESS*2)){
			
			player.tailLength += 75;
			pickup.relocate();
		}
		
	}
	
}
