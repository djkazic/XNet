package blocks;

import main.Core;
import peer.Peer;

public class BlockedFileDL implements Runnable {

	private BlockedFile bf;
	
	public BlockedFileDL(BlockedFile bf) {
		this.bf = bf;
		//TODO: write this class
	}

	public void run() {
		//Broadcast need for block #
		int blockNumber = bf.getSendBlockNumber();
		//TODO: reconsider block # vs block
		//Make BlockAcceptor
		BlockAcceptor ba = new BlockAcceptor(null, null, bf, 0);
	}
	
	private void broadcast() {
		for(Peer peer : Core.peerList) {
			
		}
	}
}
