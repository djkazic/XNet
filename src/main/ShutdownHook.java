package main;

import java.io.File;
import java.io.PrintWriter;

import peer.Peer;

/**
 * Executes all functions at ShutDown
 */
public class ShutdownHook implements Runnable {
	
	private File peerConfig;
	
	public void run() {
		Thread.currentThread().setName("ShutdownHook");
		//Save peers.dat
		try {
			peerConfig = new File(Utils.defineConfigDir() + "/" + "peers.dat");
			if(!peerConfig.exists()) {
				peerConfig.createNewFile();
			} else {
				PrintWriter writer = new PrintWriter(peerConfig, "UTF-8");
				for(Peer peer : Core.peerList) {
					String peerAddr = peer.ps.getRemoteSocketAddress().toString();
					//peerAddr = peerAddr.substring(1, peerAddr.length() - 6);
					int slashDex = peerAddr.indexOf("/");
					peerAddr = peerAddr.substring(0, slashDex);
					System.out.println("Writing peerAddr: " + peerAddr);
					writer.println(peerAddr);
				}
				writer.close();
			}
		} catch (Exception e) { e.printStackTrace(); }
		//Save downloadList
	}
}
