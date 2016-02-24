package engine;

import javax.sound.sampled.Clip;

import utils.Utils;

public class Sound {
	
	/**
	 * Set this to false if running server without gui - won't create any
	 * Clip objects or attempt to play any sounds
	 */
	public static boolean soundEnabled = true;
	public static double soundVolume = 0;
	
	public static void loopMusic(String fn, double volume){
		if(!soundEnabled)
			return; 
		
		Clip c = Resources.getSound(fn);
		Utils.setClipVolume(c, volume);
		c.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	public static void playEffect(String fn){
		if(!soundEnabled)
			return; 
		
		Clip c = Resources.getSound(fn);
		Utils.setClipVolume(c, soundVolume);
		c.start();
	}
	
	public static Clip startLoop(String fn){
		if(!soundEnabled)
			return null; 
		
		Clip c = Resources.getSound(fn);
		c.loop(Clip.LOOP_CONTINUOUSLY);
		Utils.setClipVolume(c, soundVolume);
		return c;
	}
	
}
