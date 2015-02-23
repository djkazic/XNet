package main;
import gui.MainWindow;
import io.FileWatcher;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import javax.swing.UIManager;

import net.SocketWaiter;
import net.GlobalListener;
import net.HolePunchUPNP;
import peer.Peer;
import peer.PeerConnector;
import blocks.BlockedFile;

public class Core {
	
	public static ArrayList <Peer> peerList;
	public static HashMap<Peer, ArrayList<String>> index;
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
		//L&F set
		try {
			if(Utils.isWindows()) {
				UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Calculate HWID
		Settings.hwid = Utils.getHWID();
		
		//Initialize vars
		Settings.version = 1.0;
		peerList = new ArrayList<Peer> ();
		blockDex = new ArrayList<BlockedFile> ();
		index = new HashMap<Peer, ArrayList<String>> ();
		potentialPeers = new ArrayList<String> ();
		discoveryLatch = new CountDownLatch(1);
		
		//GUI inits
		mainWindow = new MainWindow();
		mainWindow.registerListeners();
		
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
		
		mainWindow.out("Configuring network...");
		
		//Hole punch
		Thread holePunchThread = new Thread(new HolePunchUPNP());
		holePunchThread.start();
		holePunchThread.join();
		
		mainWindow.resetTable();
		mainWindow.searchInput.setFocusable(true);
		mainWindow.searchInput.setEditable(true);
		
		//Local development tools
		//debugServer = false;
		//int sep = 1;
		
		//if(sep == 0) {
			gl = new GlobalListener();
			(new Thread(gl)).start();
		//} else {
			pst = new PeerConnector(debugServer);
		//	Core.potentialPeers.add("127.0.0.1");
			(new Thread(pst)).start();
			//TODO: remove debugging
		//	Core.discoveryLatch.countDown();
		//}
	}
	
	public static void incomingDebugReset() {
		killPeerConnector = true;
		mainWindow.debugLatch.countDown();
		debugServer = false;
		mainWindow.resetTable();
	}
}
