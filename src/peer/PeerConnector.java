package peer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import main.Core;
import main.Utils;

public class PeerConnector implements Runnable {
	
	public CountDownLatch debugLatch;
	private String host = null;
	
	public PeerConnector(boolean debugServer) {
		Utils.print(this, "INITIALIZING >> PCONN");
		if(debugServer) {
			Utils.print(this, "Debug mode active, prompting connection");
			Core.mainWindow.out("Please enter the debug server IP");
			debugLatch = new CountDownLatch(1);
			Core.mainWindow.setDebugLatch(debugLatch);
			try {
				debugLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			host = Core.mainWindow.debugHost;
		}
	}
	
	public void run() {
		int attempts = 0;
		try {
			Core.discoveryLatch.await();
		} catch (InterruptedException e1) {e1.printStackTrace();}
		while(!Core.killPeerConnector && Core.potentialPeers.size() > 0) {
			//Pick a host from the potentialPeers
			host = Core.potentialPeers.get(0);
			host = host.substring(1, host.length());
			//If attempted, show marker
			if(attempts > 0) {
				Utils.print(this, "Attempting outgoing connection to potential peer " + host + " (x" + attempts + ")");
			} else {
				Utils.print(this, "Attempting outgoing connection to potential peer " + host);
			}
			//If already attempted 5 times, remove from potential peers
			if(attempts == 5) {
				Core.potentialPeers.remove(0);
			}
			attempts++;
			Socket peerSocket = new Socket();
			InetSocketAddress peerAddr = new InetSocketAddress(host, 26606);
			try {
				long start = System.currentTimeMillis();
				peerSocket.connect(peerAddr);
				long end = System.currentTimeMillis();
				Utils.print(this, "Creating peer [out]");
				(new Thread(new Peer(peerSocket, end - start, 0))).start();
				Utils.print(this, "Established connection");
				Core.killPeerConnector = true;
			} catch (IOException e) {}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Utils.print(this, "PeerSeeker terminated");
	}
}