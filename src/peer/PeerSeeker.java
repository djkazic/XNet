package peer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import main.Core;

public class PeerSeeker implements Runnable {
	
	public CountDownLatch debugLatch;
	private String host = null;
	
	public PeerSeeker(boolean debugServer) {
		System.out.println("INITIALIZING PS");
		if(debugServer) {
			System.out.println("Debug mode active, prompting connection");
			Core.mainWindow.out("Please enter the debug server IP");
			debugLatch = new CountDownLatch(1);
			Core.mainWindow.setDebugLatch(debugLatch);
			try {
				debugLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			host = Core.mainWindow.debugHost;
		} else {
			host = "127.0.0.1";
		}
	}
	
	public void run() {
		while(!Core.foundOutgoing) {
		//TODO: change found variable to "done iterating"
			System.out.println("Attempting hardcode connect to host " + host);
			Socket peerSocket = new Socket();
			InetSocketAddress peerAddr = new InetSocketAddress(host, 26606);
			try {
				long start = System.currentTimeMillis();
				peerSocket.connect(peerAddr);
				long end = System.currentTimeMillis();
				System.out.println("Creating peer [out]");
				(new Thread(new Peer(peerSocket, end - start, 0))).start();
				System.out.println("Established connection");
				Core.foundOutgoing = true;
			} catch (IOException e) {}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("PeerSeeker terminated");
	}
}