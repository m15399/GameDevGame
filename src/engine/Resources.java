package engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Lets you load resources and access them by name.
 */
public class Resources {

	//
	// Preloaded Resources
	//
	// While not necessary, we can load some resources when the program starts
	// to reduce load times
	//

	private static final String[] preloadImages = {

	};

	private static final String[] preloadSounds = {

	};

	private static final String[] preloadFiles = {

	};

	// Library of reources
	private static HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private static HashMap<String, Clip> sounds = new HashMap<String, Clip>();
	private static HashMap<String, Scanner> files = new HashMap<String, Scanner>();

	// Preload all resources
	public static void preloadResources() {
		for (int i = 0; i < preloadImages.length; i++) {
			loadImage(preloadImages[i]);
		}
		for (int i = 0; i < preloadSounds.length; i++) {
			loadSound(preloadSounds[i]);
		}
		for (int i = 0; i < preloadFiles.length; i++) {
			loadFile(preloadFiles[i]);
		}
	}

	// Sound system likely needs to be rewritten because the same Clip can't 
	// be played twice at the same time, and we can't play mp3s. 
	/**
	 * @return A reference to the sound located in Resources/filename. The sound
	 *         will be loaded if it's not already
	 */
	public static Clip getSound(String filename) {
		if (!sounds.containsKey(filename))
			loadSound(filename);
		Clip clip = sounds.get(filename);

		if (clip.isActive())
			return clip;
		clip.close();
		loadSound(filename);
		clip = sounds.get(filename);
		if (clip == null) {
			Utils.fatal("Couldn't get clip: " + filename);
		}
		return clip;
	}

	/**
	 * @return A reference to the image located in Resources/filename. The image
	 *         will be loaded if it's not already
	 */
	public static BufferedImage getImage(String filename) {
		if (!images.containsKey(filename))
			loadImage(filename);
		BufferedImage image = images.get(filename);

		if (image == null) {
			Utils.fatal("Couldn't get image: " + filename);
		}
		return image;
	}

	/**
	 * @return A reference to the file located in Resources/filename. The file
	 *         will be loaded if it's not already
	 */
	public static Scanner getFile(String filename) {
		if (!files.containsKey(filename))
			loadFile(filename);
		Scanner reader = files.get(filename);

		if (reader == null) {
			Utils.fatal("Couldn't get file: " + filename);
		}
		return reader;
	}

	
	
	private static void loadImage(String filename) {
		try {
			BufferedImage img = ImageIO.read(new File("Resources/" + filename));
			images.put(filename, img);
		} catch (IOException e) {
			Utils.fatal("Couldn't load image: " + filename);
		}
	}

	private static void loadSound(String filename) {
		// specify the sound to play
		// (assuming the sound can be played by the audio system)
		File soundFile = new File("Resources/" + filename);
		AudioInputStream sound;

		Clip clip = null;

		try {
			sound = AudioSystem.getAudioInputStream(soundFile);

			// load the sound into memory (a Clip)
			DataLine.Info info = new DataLine.Info(Clip.class,
					sound.getFormat());
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(sound);

		} catch (UnsupportedAudioFileException e) {
			Utils.fatal("Unsupported audio format: " + filename);
		} catch (IOException e) {
			Utils.fatal("Couldn't load clip: " + filename);
		} catch (LineUnavailableException e) {
			Utils.fatal("Couldn't load clip: " + filename);
		}

		sounds.put(filename, clip);

	}

	private static void loadFile(String filename) {
		try {
			Scanner s = new Scanner(new File("Resources/" + filename));
			files.put(filename, s);
		} catch (FileNotFoundException e) {
			Utils.fatal("Couldn't load clip: " + filename);
		}
	}

}
