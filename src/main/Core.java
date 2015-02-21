package main;
import gui.MainWindow;
import gui.mac.MacMainWindow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import javax.swing.UIManager;

import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;

import net.FileListener;
import net.GlobalListener;
import peer.Peer;
import peer.PeerConnector;
import blocks.BlockedFile;

public class Core {
	
	public static double version;
	public static ArrayList <Peer> peerList;
	public static HashMap<Peer, ArrayList<String>> index;
	public static MainWindow mainWindow;
	public static ArrayList<BlockedFile> blockDex;
	public static String hwid = "";
	public static ArrayList <String> potentialPeers;
	
	public static GlobalListener gl;
	public static PeerConnector pst;
	
	public static boolean debugServer = true;
	public static boolean killPeerConnector = false;
	public static CountDownLatch discoveryLatch;
	
	public static boolean firstBlockServerSocket = true;
	public static FileListener ssm;
	
	public static long chunkSize = 122880; //122.88kb blocks
	
	public static void main(String[] args) throws InterruptedException {
		//L&F init
		try {
			if(Utils.isWindows()) {
				org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
				UIManager.put("RootPane.setupButtonVisible" , false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Calculate HWID
		hwid = Utils.getHWID();
		
		//Initialize vars
		version = 1.0;
		peerList = new ArrayList<Peer> ();
		blockDex = new ArrayList<BlockedFile> ();
		index = new HashMap<Peer, ArrayList<String>> ();
		potentialPeers = new ArrayList<String> ();
		discoveryLatch = new CountDownLatch(1);
		
		//GUI inits
		if(Utils.isWindows()) {
			mainWindow = new MainWindow();
		} else {
			mainWindow = new MacMainWindow();
		}
		mainWindow.registerListeners();
		
		//Directory work
		Utils.initDir();
		
		mainWindow.out("Loading checksum data, please wait...");
		
		//Create blockdex
		try {
			Utils.generateBlockDex();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Local development tools
		//debugServer = false;
		int sep = 0;

		resetTable();
		
		//if(sep == 0) {
			gl = new GlobalListener();
			(new Thread(gl)).start();
		//} else {
			//debugServer = false;
			pst = new PeerConnector(debugServer);
			//Core.potentialPeers.add("127.0.0.1");
			(new Thread(pst)).start();
			//TODO: remove debugging
			//Core.discoveryLatch.countDown();
		//}
	}
	
	public static void sortPeers() {
		Collections.sort(peerList);
	}
	
	public static String peersCount() {
		int in = 0; 
		int out = 0;
		for(Peer peer : peerList) {
			if(peer.inout == 1) {
				in++;
			} else if(peer.inout == 0) {
				out++;
			}
		}
		return "["+ in +"|"+ out +"]";
	}

	public static boolean checkHWID(String hwid) {
		int hCount = 0;
		for(Peer peer : peerList) {
			if(peer.hwid.equals(hwid)) {
				hCount++;
			}
		}
		if(hCount > 1) {
			return false;
		}
		return true;
	}
	
	public static void incomingDebugReset() {
		killPeerConnector = true;
		mainWindow.debugLatch.countDown();
		debugServer = false;
		resetTable();
	}
	
	public static void resetTable() {
		mainWindow.out("Enter your search query and press Enter.");
	}
}
