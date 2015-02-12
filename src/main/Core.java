package main;
import java.util.ArrayList;

import net.GlobalListener;
import peer.Peer;
import peer.PeerSeeker;

public class Core {
	
	public static ArrayList <Peer> peerList;
	public static int version = 1;
	
	public static void main(String[] args) throws InterruptedException {
		peerList = new ArrayList <Peer>();
		
		boolean debugServer = false;
	
		if(debugServer) {
			GlobalListener gl = new GlobalListener();
			(new Thread(gl)).start();
			Thread.sleep(6500);
		} else {
			PeerSeeker pst = new PeerSeeker();
			(new Thread(pst)).start();
			Thread.sleep(250);
			System.out.println(peerList.get(0).getVersion());
		}
	}
}
