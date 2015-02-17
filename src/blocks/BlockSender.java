package blocks;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

import main.Core;
import main.Utils;
import peer.Peer;

//TODO: Not yet fixed
/**
 * Sends blocks after receiving block request and confirming we have it
 * @author caik
 *
 */
public class BlockSender implements Runnable {

	private Peer targetPeer;
	private File sending;
	private CountDownLatch socketDone;
	private int blockPos;
	private boolean fullFile;

	public BlockSender(Peer peer, File sending, int blockPos, boolean fullFile) {
		this.targetPeer = peer;
		this.sending = sending;
		this.blockPos = blockPos;
		this.fullFile = fullFile;
	}

	public void run() {
		try {
			socketDone = new CountDownLatch(1);
			targetPeer.createFS(socketDone);
			socketDone.await();
			if(blockPos != -1) {
				Utils.print(this, "Sending block from full-file: " + blockPos);
			} else {
				Utils.print(this, "Sending direct block " + sending.getName());
			}
			System.out.println("Outgoing file socket connected");
			DataOutputStream dos = new DataOutputStream(targetPeer.fs.getOutputStream());
			if(!fullFile) {
				System.out.println("Block method activated");
				FileInputStream fis = new FileInputStream(sending);
				//Need filesize to be sent just in case block is smaller
				byte[] buffer = new byte[(int) Core.chunkSize];
				while(fis.read(buffer) > 0) {
					dos.write(buffer);
				}
				dos.flush();
				fis.close();
			} else {
				System.out.println("RAF method activated");
				RandomAccessFile raf = new RandomAccessFile(sending, "r");
				raf.seek(Core.chunkSize * blockPos); //position of block to send
				byte[] buffer = new byte[(int) Core.chunkSize];
				//Potentially do this by having a totalCount, reading in 4096 bytes each time
				Utils.print(this, "Writing buffer to DataOutputStream");
				if(raf.read(buffer) > 0) {
					dos.write(buffer);
				}
				dos.flush();
				raf.close();
			}
			dos.close();
			targetPeer.fs.close();
		} catch (Exception e) {}
	}
}
