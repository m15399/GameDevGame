package server;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

import engine.Observer;
import engine.Utils;

/**
 * Prints log info that would normally be written to stdout/stderr to a log file, 
 * and to a gui window if desired
 */
public class Logger {

	private JFrame frame;
	private JTextArea textArea;
	
	private PrintWriter logFile;	
	
	public Logger(boolean gui){
		
		// Redirect Utils.log and Utils.err to here
		Utils.logObserver = new Observer(){
			public void notify(Object arg) {
				log((String)arg);
			}
		};
		Utils.errObserver = new Observer(){
			public void notify(Object arg) {
				log((String)arg);
			}
		};
		Utils.fatalObserver = new Observer(){
			public void notify(Object arg){
				quit(1);
			}
		};
		
		// Try to close file when shutting down
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
				if(logFile != null)
					logFile.close();
		    }
		}));
		
		
		// Create the window if requested
		
		frame = null;
		textArea = null;
		
		if(gui){
			frame = new JFrame();
			
			frame.setLayout(new BorderLayout());
			
			frame.setTitle("GameDevGame Server");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter()
			{
			    public void windowClosing(WindowEvent e)
			    {
			        quit(0);
			    }
			});
			
			frame.setSize(600, 200);
			frame.setLocation(100, 100);
			
			textArea = new JTextArea();
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setAutoscrolls(true);
			
			int margin = 5;
			textArea.setMargin(new Insets(margin, margin, margin, margin));

			JScrollPane pane = new JScrollPane(textArea);

			frame.add(pane, BorderLayout.CENTER);
			
			frame.setVisible(true);
		}
		
		// Create log file
		
		Date date = new Date();
		String dateString = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(date);
		String filename = "ServerLog_" + dateString + ".txt";
		
		logFile = null;
		
		// Make the logs/ directory if not there
		File logsDir = new File("logs/");
		boolean dirExists = logsDir.exists();
		if(!dirExists)
			dirExists = logsDir.mkdir();
		
		// Make the file
		if(dirExists){
			try {
				logFile = new PrintWriter(new File("logs/" + filename));
			} catch (FileNotFoundException e) {
				Utils.err("Unable to create log file");
			}
		} else {
			Utils.err("Unable to create logs/ directory, running without log file");
		}
		
	}
	
	private void quit(int status){
		if(logFile != null)
			logFile.close();
		frame.dispose();
		System.exit(status);
	}
	
	private void log(String s){
		Date date = new Date();
		String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date);
		String logString = "[" + dateString + "]: " + s + "\n";
		
		if(textArea != null){
			textArea.append(logString);
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
		
		if(logFile != null){
			logFile.print(logString);
			logFile.flush();
		}
	}

}
