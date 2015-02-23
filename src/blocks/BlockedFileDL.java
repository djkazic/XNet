package blocks;

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
		while(bf.getNeededBlock() != null) {
			broadcast(bf.getName(), bf.getNeededBlock());
			try {
				Thread.sleep(800);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			bf.unify();
		} catch (Exception e) {
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
	public BlockedFile getBfInstance() {
		return bf;
	}
}
