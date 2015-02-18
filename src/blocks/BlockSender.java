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
	private int fileSize;

	public BlockSender(Peer peer, File sending, int blockPos, int fileSize) {
		this.targetPeer = peer;
		this.sending = sending;
		this.blockPos = blockPos;
		this.fileSize = fileSize;
	}

	public void run() {
		try {
			//Calculate fileSize if not sending direct
			if(blockPos != -1) {
				Utils.print(this, "Sending block from full-file: " + blockPos);
				RandomAccessFile raf = new RandomAccessFile(sending, "r");
				raf.seek(Core.chunkSize * blockPos); //position of block to send
				byte[] buffer = new byte[(int) Core.chunkSize];
				fileSize = raf.read(buffer);
				raf.close();
			}
			
			System.out.println("BS Created: fileSize: " + fileSize);
			
			//Send rest of response code (fileSize)
			targetPeer.dos.writeInt(fileSize);
			targetPeer.dos.flush();
			
			socketDone = new CountDownLatch(1);
			targetPeer.createFS(socketDone);
			socketDone.await();
			System.out.println("Outgoing file socket connected");
			DataOutputStream dos = new DataOutputStream(targetPeer.fs.getOutputStream());
			
			if(blockPos != -1) {
				System.out.println("Temp blocking method activated");
				sending = Utils.getTempBlock(sending, blockPos);
			} else {
				System.out.println("Block method activated");
			}
			
			FileInputStream fis = new FileInputStream(sending);
			//Need filesize to be sent just in case block is smaller
			byte[] buffer = new byte[(int) Core.chunkSize];
			while(fis.read(buffer) > 0) {
				dos.write(buffer);
			}
			dos.flush();
			fis.close();
			System.out.println("Finished!");
			Thread.sleep(50);
			dos.close();
			targetPeer.fs.close();
		} catch (Exception e) {}
	}
}
