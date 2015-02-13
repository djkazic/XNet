package main;
import gui.MainWindow;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import net.GlobalListener;
import peer.Peer;
import peer.PeerSeeker;

public class Core {
	
	public static int version;
	public static ArrayList <Peer> peerList;
	public static ArrayList<String> plainText;
	public static HashMap<Peer, String[]> index;
	public static MainWindow mainWindow;
	
	public static void main(String[] args) throws InterruptedException {
		//Initialize vars
		version = 1;
		peerList = new ArrayList <Peer>();
		plainText = new ArrayList <String>();
		
		//Directory work
		Utils.initDir();
		
		//GUI init
		mainWindow = new MainWindow();
		
		boolean debugServer = false;
		try {
			System.out.println(Utils.listDir(""));
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(debugServer) {
			GlobalListener gl = new GlobalListener();
			(new Thread(gl)).start();
			Thread.sleep(10500);
		} else {
			PeerSeeker pst = new PeerSeeker();
			(new Thread(pst)).start();
			Thread.sleep(10500);
			peerList.get(0).st.requestNameList("sk");
			
		}
	}
	
	public static void sortPeers() {
		Collections.sort(peerList);
	}
}
