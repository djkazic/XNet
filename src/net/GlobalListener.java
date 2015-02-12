package net;
import java.net.ServerSocket;
import java.net.Socket;

import peer.Peer;

public class GlobalListener implements Runnable {
	public ServerSocket ss;
	
	public GlobalListener() {
		System.out.println("GL");
		try {
			ss = new ServerSocket(26606);
		} catch (Exception e) {
			System.out.println("Global listener failed");
			e.printStackTrace();
		}
	}
	
	public void run() {
		Socket tempSocket;
		while(true) {
			try {
				tempSocket = ss.accept();
				System.out.println("Creating peer [in]");
				(new Thread(new Peer(tempSocket))).start();
			} catch (Exception e) {  }
		}
	}

}
