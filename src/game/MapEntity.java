package game;

import engine.Entity;

/**
 * A MapEntity is an Entity that lives on the map. It's different from an Entity
 * in that it gets drawn beneath things that are "south" of it.
 * 
 * For example, if the player is just below a wall, his sprite gets drawn on top
 * of the wall. But when he goes behind the wall, part of him gets covered up by
 * the wall.
 * 
 * We fix this by slightly modifying the Entity's draw order... this is a bit
 * dangerous, but it fixes the problem. We could add another field like
 * "orderInLayer" later if needed.
 * 
 * Remember to call super.update(dt) for anything that extends MapEntity!!
 */
public class MapEntity extends Entity {

	private double mapDrawOrder;

	public MapEntity() {
		mapDrawOrder = super.getDrawOrder();
	}

	public void update(double dt) {
		super.setDrawOrder(mapDrawOrder + y / 1000000.0);
	}

	public void setDrawOrder(double o) {
		mapDrawOrder = o;
	}

	public double getDrawOrder() {
		return mapDrawOrder;
	}
}
