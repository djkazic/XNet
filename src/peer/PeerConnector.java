package peer;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import main.Core;
import main.Utils;

public class PeerConnector implements Runnable {
	
	public CountDownLatch debugLatch;
	private String host = null;
	private File peerConfig;
	
	public PeerConnector(boolean debugServer) {
		Utils.print(this, "INITIALIZING >> PCONN");
		if(debugServer) {
			Utils.print(this, "Debug mode active, prompting connection");
			Core.mainWindow.out("Please enter the debug server IP");
			debugLatch = new CountDownLatch(1);
			Core.mainWindow.setDebugLatch(debugLatch);
		}
	}
	
	public void run() {
		try {
			if(!Core.debugServer) {
				//Check config
				peerConfig = new File(Utils.defineConfigDir() + "/" + "peers.dat");
				if(!peerConfig.exists()) {
					peerConfig.createNewFile();
				} else {
					ArrayList<String> potentialPeersFromFile = new ArrayList<String> ();
					potentialPeersFromFile = (ArrayList<String>) Files.readAllLines(peerConfig.toPath(), Charset.defaultCharset());
					System.out.println("Peers from file: " + potentialPeersFromFile);
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		//Scan for local peers
		//(new Thread(new DiscoveryServer())).start();
		//(new Thread(new DiscoveryThread())).start();
		int attempts = 0;
		if(Core.debugServer) {
			try {
				debugLatch.await();
			} catch (InterruptedException e) { e.printStackTrace(); }
			Core.potentialPeers.add(Core.mainWindow.debugHost);
		} else {
			try {
				Core.discoveryLatch.await();
			} catch (InterruptedException e1) {e1.printStackTrace();}
		}
		while(!Core.killPeerConnector && Core.potentialPeers.size() > 0) {
			//Pick a host from the potentialPeers
			host = Core.potentialPeers.get(0);
			String phost = "";
			if(host.startsWith("/")) {
				phost = host.substring(1, host.length());
			} else {
				phost = host;
			}
			//If attempted, show marker
			if(attempts > 0) {
				Utils.print(this, "Attempting outgoing connection to potential peer " + phost + " (x" + attempts + ")");
			} else {
				Utils.print(this, "Attempting outgoing connection to potential peer " + phost);
			}
			//If already attempted 5 times, remove from potential peers
			if(attempts == 5) {
				Core.potentialPeers.remove(host);
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
				Core.potentialPeers.remove(host);
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