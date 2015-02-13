package net;
import java.net.ServerSocket;
import java.net.Socket;

import peer.Peer;

public class GlobalListener implements Runnable {
	public ServerSocket ss;
	
	public GlobalListener() {
		System.out.println("INITIALIZING GL");
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
				long start = System.currentTimeMillis();
				tempSocket = ss.accept();
				long end = System.currentTimeMillis();
				System.out.println("Creating peer [in]");
				(new Thread(new Peer(tempSocket, end - start))).start();
			} catch (Exception e) {  }
		}
	}

}
