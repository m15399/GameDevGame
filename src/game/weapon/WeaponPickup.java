package game.weapon;

import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import utils.Utils;

import engine.Game;
import game.Globals;
import game.MapEntity;
import game.Player;
import game.CollisionManager.Collider;

/**
 * Lets you create pickups for different weapons. The pickup properties - 
 * class, image, and respawn time - are set in this file. 
 */
public class WeaponPickup extends MapEntity implements Collider {

	//
	// Describe pickup types
	//
	
	private static class WeaponData {
		public String imageFileName;
		public double respawnTime; 
		
		public WeaponData(String fn, double respTime){
			imageFileName = fn;
			respawnTime = respTime;
		}
	}
	
	private static final HashMap<Class<? extends Weapon>, WeaponData> weaponDataMap;
	static {
		weaponDataMap = new HashMap<Class<? extends Weapon>, WeaponData>();
		
		//
		// Put pickup descriptions here:
		// (fill the map of: Class -> WeaponData)
		//
		weaponDataMap.put(BowAndArrow.class, new WeaponData("pickup_bow.png", 10));
		weaponDataMap.put(Sword.class, new WeaponData("pickup_sword.png", 10));
	}
	
	
	// Floaty appearance of the pickup
	private static final double FLOAT_PERIOD = 1;
	private static final double FLOAT_HEIGHT = 3;
	
	
	private Class<? extends Weapon> weaponClass;
	private String imageFile;
	private boolean visible = true;
	private double respawnTimeLeft = 0, respawnTime;
	private double floatOffset = Math.random() * FLOAT_PERIOD; // so we don't all float in sync
	
	
	public WeaponPickup(Class<? extends Weapon> weaponClass, double x, double y){
		WeaponData data = weaponDataMap.get(weaponClass);
		if(data == null){
			Utils.fatal("Unable to create pickup - no WeaponData was specified for: " + weaponClass);
		}
		imageFile = data.imageFileName;
		respawnTime = data.respawnTime;
		
		this.weaponClass = weaponClass;
		this.x = x;
		this.y = y;
	}

	public boolean collidesWithPlayer(Player p) {
		return p.touchesCircle(x, y, 12);
	}

	public void onPlayerCollision(Player p) {
		pickup(p);
	}
	
	private void pickup(Player p){
		if(!visible)
			return;
		
		// Give player this weapon
		try {
			// The weapon is an instance of 'weaponClass'
			Constructor<? extends Weapon> constructor = weaponClass.getConstructor(Player.class);
			Weapon wep = constructor.newInstance(new Object[]{ p });
			p.setWeapon(wep);
			
		} catch (Exception e) { 
			Utils.fatal("Unable to locate weapon constructor"); 
		}
		
		// Pick it up - make invisible and set respawn timer
		respawnTimeLeft = respawnTime;
		visible = false;
	}
	
	public void update(double dt){
		super.update(dt);
		
		if(!visible){
			respawnTimeLeft -= dt;
			if(respawnTimeLeft <= 0){
				// respawn
				visible = true;
			}
		} else {
			Globals.collisionManager.addColliderForFrame(this);
		}
	}
	
	public void draw(Graphics2D g){
		if(!visible)
			return;
		
		// oscillates -1 to 1
		double floatAmplitude = Math.sin(
				(Game.time + floatOffset) * (Math.PI * 2) / FLOAT_PERIOD);
		
		// shadow
		g.setColor(new Color(0, 0, 0, .4f));
		int shadowWidth = (int)(10 - 2.5 * floatAmplitude);
		int shadowHeight = (int)(4 - .25 * floatAmplitude);
		g.fillRect((int)(x-shadowWidth), (int)y + 10 - shadowHeight, shadowWidth*2, shadowHeight*2);
		
		// image
		double floatAmt = floatAmplitude * FLOAT_HEIGHT;
		Utils.drawImage(g, imageFile, x, y - floatAmt - 16, 0, 1);
	}

}
