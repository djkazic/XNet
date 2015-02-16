package net;
import java.net.ServerSocket;
import java.net.Socket;

import main.Utils;
import peer.Peer;

public class GlobalListener implements Runnable {
	public ServerSocket ss;
	public ServerSocket fs;
	public Socket fsSocket;
	
	public GlobalListener() {
		Thread.currentThread().setName("GlobalListener");
		Utils.print(this, "INITIALIZING >> GL");
		try {
			ss = new ServerSocket(26606);
		} catch (Exception e) {
			Utils.print(this, "Global listeners failed");
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
				Utils.print(this, "Creating peer [in]");
				(new Thread(new Peer(tempSocket, end - start, 1))).start();
			} catch (Exception e) {  }
		}
	}
}