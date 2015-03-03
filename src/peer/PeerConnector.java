package peer;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import main.Core;
import main.Utils;

public class PeerConnector implements Runnable {
	
	public CountDownLatch debugLatch;
	private String originalHost = null;
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
		(new Thread(new DiscoveryServer())).start();
		(new Thread(new DiscoveryThread())).start();
		int attempts = 0;
		if(Core.debugServer) {
			try {
				debugLatch.await();
			} catch (InterruptedException e) { e.printStackTrace(); }
			Core.potentialPeers.add(Core.mainWindow.debugHost);
		} else {
			try {
				//Allocate 8 seconds for local peer discovery
				Thread.sleep(8000);
			} catch (InterruptedException e1) {e1.printStackTrace();}
		}
		while(!Core.killPeerConnector || Core.potentialPeers.size() > 0) {
			//If we have no more hosts, wait a cycle
			if(Core.potentialPeers.size() == 0) {
				Utils.print(this, "No current hosts, thread sleeping");
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			//Pick a host from the potentialPeers
			originalHost = Core.potentialPeers.get(0);
			String connectHost = originalHost;
			//Process port, if there is one
			int port = 26606;
			if(connectHost.contains(":")) {
				String[] split = connectHost.split(":");
				connectHost = split[0];
				port = Integer.parseInt(split[1]);
			}
			//Readability fix
			String printHost = "";
			if(connectHost.startsWith("/")) {
				printHost = connectHost.substring(1, connectHost.length());
			} else {
				printHost = connectHost;
			}
			//If attempted, show marker
			if(attempts > 0) {
				Utils.print(this, "Attempting outgoing connection to potential peer " + printHost + ":" + port + " (x" + attempts + ")");
			} else {
				Utils.print(this, "Attempting outgoing connection to potential peer " + printHost + ":" + port);
			}
			//If already attempted 10 times, remove from potential peers
			if(attempts == 9) {
				Core.potentialPeers.remove(originalHost);
				attempts = -1;
			}
			attempts++;
			Socket peerSocket = new Socket();
			try {
				peerSocket.setSoTimeout(1000);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
			InetSocketAddress peerAddr = new InetSocketAddress(connectHost, port);
			try {
				long start = System.currentTimeMillis();
				peerSocket.connect(peerAddr, 1000);
				long end = System.currentTimeMillis();
				Utils.print(this, "Creating peer [out]");
				(new Thread(new Peer(peerSocket, end - start, 0))).start();
				Utils.print(this, "Established connection");
				Core.potentialPeers.remove(originalHost);
			} catch (IOException e) {}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Utils.print(this, "PeerSeeker terminated");
	}
}