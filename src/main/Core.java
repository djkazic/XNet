package main;
import gui.MainWindow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.GlobalListener;
import peer.Peer;
import peer.PeerSeeker;

public class Core {
	
	public static double version;
	public static ArrayList <Peer> peerList;
	public static HashMap<Peer, String[]> index;
	public static MainWindow mainWindow;
	public static String md5dex = "";
	public static String hwid = "";
	public static ArrayList <String[]> fileToHash;
	
	public static GlobalListener gl;
	public static PeerSeeker pst;
	
	public static boolean debugServer = true;
	public static boolean foundOutgoing = false;
	
	public static void main(String[] args) throws InterruptedException {
		//Calculate HWID
		hwid = Utils.getHWID();
		
		//Initialize vars
		version = 1.0;
		peerList = new ArrayList <Peer>();
		fileToHash = new ArrayList <String[]> ();
		index = new HashMap<Peer, String[]> ();
		
		//Directory work
		Utils.initDir();

		//GUI init
		mainWindow = new MainWindow();
		mainWindow.out("Loading md5sum data, please wait...");
		
		//Create md5dex
		try {
			md5dex = Utils.listDir();
			System.out.println(md5dex);
		} catch (Exception e) {
			e.printStackTrace();
		}

		resetTable();
		debugServer = false;
		int sep = 1;
		if(sep == 0) {
			gl = new GlobalListener();
			(new Thread(gl)).start();
		} else {
			//debugServer = false;
			pst = new PeerSeeker(debugServer);
			(new Thread(pst)).start();
		}
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
		foundOutgoing = true;
		mainWindow.debugLatch.countDown();
		debugServer = false;
		resetTable();
	}
	
	public static void resetTable() {
		mainWindow.out("Enter your search query and press Enter.");
	}
}
