package blocks;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;
import main.Core;
import main.Utils;
import peer.Peer;

//Pumps the file through to peer
public class BlockSender implements Runnable {

	private Peer targetPeer;
	private File sending;
	private int sendingBlock;
	private CountDownLatch socketDone;

	public BlockSender(Peer peer, String file, int sendingBlock) {
		this.targetPeer = peer;
		this.sendingBlock = sendingBlock;
		sending = Utils.findBySum(file);
	}

	public void run() {
		try {
			socketDone = new CountDownLatch(1);
			targetPeer.createFS(socketDone);
			socketDone.await();
			System.out.println("Outgoing file socket connected");
			DataOutputStream dos = new DataOutputStream(targetPeer.fs.getOutputStream());
			FileInputStream fis = new FileInputStream(sending);
			//Need filesize to be sent just in case block is smaller
			byte[] buffer = new byte[(int) Core.chunkSize];
			int blockNumber = sendingBlock;
			for(int i=0; i <= blockNumber; i++) {
				fis.read(buffer);
			}
			//TODO: dos size
			dos.flush();
			fis.close();
			dos.close();
			targetPeer.fs.close();
		} catch (Exception e) {}
	}
}
