package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Lets you map class names to int's. You must register all classes BEFORE
 * getting any class numbers. Otherwise the list will cannot be generated correctly. 
 */
public class ClassToIntMapper {

	private ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
	private HashMap<Class<?>, Integer> classToIntMap = null;
	
	/**
	 * Add a class
	 */
	public void registerClass(Class<?> theClass){
		classes.add(theClass);
	}
	
	/**
	 * Get the number of the class
	 */
	public int getClassNumber(Class<?> theClass){
		// If we haven't generated the map, generate it
		if(classToIntMap == null){
			classToIntMap = new HashMap<Class<?>, Integer>();
			
			// Sort classes by name
			Collections.sort(classes, new Comparator<Class<?>>(){
				public int compare(Class<?> o1, Class<?> o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			int currId = 0;
			for(Class<?> c : classes){
				classToIntMap.put(c, currId++);
			}
		}
		
		// Look up the class's number
		int mapping = -1;
		try {
			mapping = classToIntMap.get(theClass);
		} catch (NullPointerException e){}
		
		return mapping;
	}
	
	public ArrayList<Class<?>> getClassList(){
		return classes;
	}
}
