package engine;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

import utils.Utils;

/**
 * GameObjects are automatically updated and drawn each frame. Most objects in
 * the game should extend from GameObject. (This is sort of modeled on how
 * Unity's GameObject system works).
 * 
 * Some things to keep in mind:
 * onStart for each object is called at the start of the frame AFTER it was 
 * created.
 * Objects are destroyed at the end of the frame they are destroyed on. This
 * is also the point where their onDestroy methods are called. 
 */
public class GameObject {
	
	public String tag;
	private double drawOrder; // e.g. backgrounds have negative draw orders
	private int objectIndex; // index in allObjects list
	
	private boolean destroyed;
	
	public GameObject() {
		newObjects.add(this);

		tag = "";
		drawOrder = 0;
		
		objectIndex = -1;
		
		destroyed = false;
	}

	//
	// Public methods
	//
	
	// Override this
	public void update(double dt) {
	}

	// Override this
	public void draw(Graphics2D g) {
	}

	/**
	 * Override this to do some code when the object is created
	 */
	public void onStart() {
	}

	/**
	 * Override this to do some code when the object is destroyed
	 */
	public void onDestroy() {
	}

	public void destroy() {
		if(destroyed)
			Utils.err("Tried to destroy object more than once");
		
		toDestroy.add(this);
		destroyed = true;
	}

	public void setDrawOrder(double o){
		drawOrder = o;
	}
	
	public double getDrawOrder(){
		return drawOrder;
	}
	
	
	//
	// Static methods for GameObject system
	//

	// List of all GameObjects in the game
	// May have empty spots, which will be set to null
	private static ArrayList<GameObject> allObjects = new ArrayList<GameObject>();
	
	private static Stack<Integer> freeSpots = new Stack<Integer>();
	
	// List of objects created this frame
	private static ArrayList<GameObject> newObjects = new ArrayList<GameObject>();

	// List of objects to destroy at end of frame
	private static ArrayList<GameObject> toDestroy = new ArrayList<GameObject>();

	

	/**
	 * Returns a list of all the GameObjects that have the given tag. Yes, this
	 * is technically slow, but it will be fine if we only call it a couple
	 * times per frame. It's very helpful for finding all the enemies, all the
	 * ground tiles, etc
	 */
	public static ArrayList<GameObject> findObejctsByTag(String tag) {
		ArrayList<GameObject> objs = new ArrayList<GameObject>();
		for (GameObject o : allObjects) {
			if (o != null && o.tag.equals(tag))
				objs.add(o);
		}
		return objs;
	}

	// Add an object to the allObjects list and call its onStart method
	private static void addObject(GameObject o){
		if(freeSpots.empty()){
			allObjects.add(o);
			o.objectIndex = allObjects.size()-1;
		} else {
			int spot = freeSpots.pop();
			allObjects.set(spot, o);
			o.objectIndex = spot;
		}
		o.onStart(); 
	}
	
	// Add all the newly created objects 
	private static void addNewObjects(){
		for (int i = 0; i < newObjects.size(); i++) {
			GameObject o = newObjects.get(i);
			addObject(o);
		}
		newObjects.clear();
	}

	// Call the object's onDestroy method and remove it from allObjects
	private static void removeObject(GameObject o){
		o.onDestroy();

		int i = o.objectIndex;
		allObjects.set(i, null);
		freeSpots.push(i);
	}
	
	// Remove all objects waiting to be destroyed
	private static void removeDestroyedObjects(){
		for(int i = 0; i < toDestroy.size(); i++){
			removeObject(toDestroy.get(i));
		}
		toDestroy.clear();
	}
	
	// Update all GameObjects in the game
	public static void updateAll(double dt) {

		addNewObjects();
		
		for (GameObject o : allObjects) {
			if(o != null && !o.destroyed)
				o.update(dt);
		}
		
//		addNewObjects();
		removeDestroyedObjects();
	}

	// Draw all GameObjects in the game
	public static void drawAll(Graphics2D g) {

		// Make a copy so we can sort it
		ArrayList<GameObject> toDraw = new ArrayList<GameObject>(allObjects);
		
		// sort objects by their drawPriority
		Collections.sort(toDraw, new Comparator<GameObject>() {
			public int compare(GameObject o1, GameObject o2) {
				if(o1 == null && o2 == null)
					return 0;
				else if(o1 == null)
					return -1;
				else if(o2 == null)
					return 1;
				
				double d = o1.drawOrder - o2.drawOrder;
				if (d == 0)
					return 0;
				else if (d < 0)
					return -1;
				else
					return 1;
			}
		});

		for(GameObject o : toDraw){
			if(o != null)
				o.draw(g);
		}
	}

	// Delete all current GameObjects, e.g. when starting a new level
	public static void destroyAllObjects() {
		for(GameObject o : allObjects){
			if(o != null)
				o.destroy();
		}
		for(GameObject o : newObjects){
			o.destroy();
		}
	}

}
