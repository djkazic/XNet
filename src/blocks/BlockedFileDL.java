package blocks;

import java.io.IOException;

import main.Core;
import main.Utils;
import peer.Peer;

//Handles download of chunks for a specific BlockedFile
public class BlockedFileDL implements Runnable {

	private BlockedFile bf;
	
	public BlockedFileDL(BlockedFile bf) {
		this.bf = bf;
		Thread.currentThread().setName("BlockedFileDL - " + bf.getName());
	}

	/**
	 * Broadcasts block requests until complete, then unifies
	 * BlockedFile
	 */
	public void run() {
		System.out.println(bf);
		String block = "";
		while((block = bf.getNeededBlock()) != null) {
			broadcast(bf.getName(), block);
			try {
				Utils.print(this, "Waiting for response...");
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			bf.unify();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void broadcast(String forFile, String blockName) {
		for(Peer peer : Core.peerList) {
			Utils.print(this, "Requesting block " + blockName + " for " + forFile);
			peer.st.requestBlock(forFile, blockName);
		}
	}
	
	/**
	 * Record block has been received
	 * @param blockName
	 */
	public void logBlock(String blockName) {
		bf.logBlock(blockName);
	}
}
