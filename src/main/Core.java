package main;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import net.GlobalListener;
import peer.Peer;
import peer.PeerSeeker;

public class Core {
	
	public static int version;
	public static ArrayList <Peer> peerList;
	
	public static void main(String[] args) throws InterruptedException {
		//Initialize vars
		version = 1;
		peerList = new ArrayList <Peer>();
		
		//Directory work
		Utils.initDir();
		
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
}
