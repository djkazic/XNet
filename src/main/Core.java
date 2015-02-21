package main;
import gui.MainWindow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import javax.swing.UIManager;
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
		//L&F set
		try {
			if(Utils.isWindows()) {
				UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
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
		mainWindow = new MainWindow();
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
		debugServer = false;
		int sep = 1;

		resetTable();
		
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
	
	public static void sortPeers() {
		Collections.sort(peerList);
	}
	
	public static String peersCount() {
		int size = peerList.size();
		if(size == 0) {
			//0 peers
			return "0bars";
		} else if(size <= 2) {
			//1 or 2 peers
			return "1bars";
		} else if(size <= 4) {
			//3 or 4 peers
			return "2bars";
		} else if(size > 4) {
			return "3bars";
		}
		return null;
	}
	
	public static String peerToolTip() {
		int inCount = 0;
		int outCount = 0;
		for(Peer peer : peerList) {
			if(peer.inout == 1) {
				inCount++;
			} else if(peer.inout == 0) {
				outCount++;
			}
		}
		return "[" + inCount + "|" + outCount + "]";
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
