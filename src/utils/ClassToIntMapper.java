package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ClassToIntMapper {

	private ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
	private HashMap<Class<?>, Integer> classToIntMap = null;
	
	public void registerClass(Class<?> theClass){
		classes.add(theClass);
	}
	
	public int getClassNumber(Class<?> theClass){
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
		
		int mapping = -1;
		
		try {
			mapping = classToIntMap.get(theClass);
		} catch (NullPointerException e){}
		
		return mapping;
	}
	
	public ArrayList<Class<?>> getClassList(){
		return classes;
	}
	
	public String getClassListString(){
		String s = "";
		s += "---\n";
		for(Class<?> theClass : classes){
			s += theClass.getName() + " : " + getClassNumber(theClass) + "\n";
		}
		s += '\n';
		return s;
	}
	
}
