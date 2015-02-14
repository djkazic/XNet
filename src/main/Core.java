package main;
import gui.MainWindow;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
	public static ArrayList <String[]> fileToHash;
	
	public static void main(String[] args) throws InterruptedException {
		//Initialize vars
		version = 1.0;
		peerList = new ArrayList <Peer>();
		fileToHash = new ArrayList <String[]> ();
		
		//Directory work
		Utils.initDir();

		//GUI init
		mainWindow = new MainWindow();
		mainWindow.out("Loading md5sum data, please wait...");
		
		//TODO: rewrite listDir dear god
		try {
			md5dex = Utils.listDir();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}

		mainWindow.out("Enter your search query and press Enter.");
		
		boolean debugServer = false;
		if(debugServer) {
			GlobalListener gl = new GlobalListener();
			(new Thread(gl)).start();
		} else {
			PeerSeeker pst = new PeerSeeker();
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
}
