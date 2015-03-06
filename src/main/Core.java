package main;

import gui.MainWindow;
import gui.WarningPopup;
import io.FileWatcher;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import net.IRCBootstrap;
import net.SocketWaiter;
import net.GlobalListener;
import net.HolePunchUPNP;
import peer.Peer;
import peer.PeerConnector;
import blocks.BlockedFile;

public class Core {
	
	public static ArrayList <Peer> peerList;
	public static HashMap<String, ArrayList<String>> index;
	public static MainWindow mainWindow;
	public static ArrayList<BlockedFile> blockDex;
	public static ArrayList <String> potentialPeers;
	public static GlobalListener gl;
	public static PeerConnector pst;
	public static SocketWaiter ssm;
	public static boolean killPeerConnector = false;
	public static boolean fsThreadStarted = false;

	public static void main(String[] args) throws InterruptedException, IOException, NoSuchAlgorithmException {		
		Utils.init();
		
		//Version check
		String strVer = System.getProperty("java.version");
		double ver = Double.parseDouble(strVer.substring(0, 3));
		if(ver < 1.7) {
			Thread warnThread = new Thread(new WarningPopup(true, "Your Java version of " + ver + " is too old! Update to Java 7."));			
			warnThread.start();
			warnThread.join();
		}
		
		//Calculate HWID
		Settings.hwid = Utils.getHWID();
		
		//Initialize vars
		Settings.version = 1.0;
		peerList = new ArrayList<Peer> ();
		blockDex = new ArrayList<BlockedFile> ();
		index = new HashMap<String, ArrayList<String>> ();
		potentialPeers = new ArrayList<String> ();
		
		//GUI inits
		mainWindow = new MainWindow();
		
		//Directory work
		Utils.initDir();
		
		//Register fileWatcher (Java 7 feature)
		(new Thread(new FileWatcher())).start();

		//Hole punch
		mainWindow.out("Configuring network...");
		Thread holePunchThread = new Thread(new HolePunchUPNP());
		holePunchThread.start();
		holePunchThread.join();
		
		//Start IRC thread
		try {
			(new Thread(new IRCBootstrap("asimov.freenode.net", "#xnetbootstrap", 6667))).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Register shutdownhook
		Thread shutDownHookThread = new Thread(new ShutdownHook());
		Runtime.getRuntime().addShutdownHook(shutDownHookThread);
		
		mainWindow.out("Loading checksum data...");
		
		//Create blockdex
		Utils.print(Core.class, "Generating blockDex");
		Utils.generateBlockDex();
		
		//Initialize library listing
		mainWindow.updateLibrary();
		
		//Allow searches
		mainWindow.resetTable();
		mainWindow.setSearchFocusable();
		mainWindow.setSearchEditable();
		
		//Local development tools
		//debugServer = false;
		int sep = 1;
		
		if(sep == 0) {
			gl = new GlobalListener();
			(new Thread(gl)).start();
		} else {
			pst = new PeerConnector(Settings.debugServer);
			Core.potentialPeers.add("127.0.0.1");
			(new Thread(pst)).start();
		}
	}
	
	public static void incomingDebugReset() {
		killPeerConnector = true;
		mainWindow.debugLatch.countDown();
		Settings.debugServer = false;
		mainWindow.resetTable();
	}
}
