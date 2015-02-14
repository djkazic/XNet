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
	public static ArrayList <String[]> fileToHash;
	
	public static boolean debugServer = true;
	
	public static void main(String[] args) throws InterruptedException {
		
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		mainWindow.out("Enter your search query and press Enter.");
		
		//if(debugServer) {
		GlobalListener gl = new GlobalListener();
		(new Thread(gl)).start();
		//} else {
		PeerSeeker pst = new PeerSeeker(debugServer);
		(new Thread(pst)).start();
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
}
