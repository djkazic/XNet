package main;
import gui.MainWindow;
import io.FileWatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
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
	public static CountDownLatch punchLatch;
	public static boolean firstBlockServerSocket = true;
	public static SocketWaiter ssm;

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
		punchLatch = new CountDownLatch(1);
		
		//GUI inits
		mainWindow = new MainWindow();
		mainWindow.registerListeners();
		
		//Directory work
		Utils.initDir();
		
		//Register fileWatcher
		(new Thread(new FileWatcher())).start();
		
		mainWindow.out("Loading checksum data, please wait...");
		
		//Create blockdex
		Utils.print("Generating blockDex");
		Utils.generateBlockDex();
		
		mainWindow.resetTable();
		
		//Hole punch
		(new Thread(new HolePunchUPNP(punchLatch))).start();
		punchLatch.await();
		
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
