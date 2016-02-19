package game;

import utils.ClassToIntMapper;
import utils.Utils;
import engine.GameObject;

public abstract class Weapon extends GameObject {
	
	private static ClassToIntMapper weaponClassToIntMapper = new ClassToIntMapper();
	
	static {
		registerClass(BowAndArrow.class);
	}
	
	private static void registerClass(Class<? extends Weapon> weaponClass){
		weaponClassToIntMapper.registerClass(weaponClass);
	}
	
	public Player player;
	
	public Weapon(Player player){
		if(getWeaponId() < 0){
			Utils.fatal("Couldn't find class: " + getClass() + 
					", you need to register each weapon in Weapon.java.");
		}
		this.player = player;
	}
	
	public int getWeaponId(){
		return weaponClassToIntMapper.getClassNumber(getClass());
	}
	
	public abstract double getChargePct();
	
	public abstract String getName();

}
