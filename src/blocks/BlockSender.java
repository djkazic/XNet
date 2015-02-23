package blocks;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;

import main.Settings;
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
	public byte[] rafBuffer;

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
				fileSize = Utils.getRAFBlock(sending, blockPos, this);
				if(fileSize == 0) {
					throw new Exception("blockPos was higher than valid");
				}
				//getRAFBlock will return fileSize but set buffer automatically
				//TODO: handle when getRAFBlock returns 0 (i.e. blockPos was too high)
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
				Utils.print(this, "Writing to DOS");
				dos.write(rafBuffer);
				Utils.print(this, "Done writing to DOS");
				dos.flush();
			} else {
				System.out.println("Block method activated");
				FileInputStream fis = new FileInputStream(sending);
				//Need filesize to be sent just in case block is smaller
				byte[] buffer = new byte[(int) Settings.blockSize];
				while(fis.read(buffer) > 0) {
					dos.write(buffer);
				}
				dos.flush();
				fis.close();
			}
			System.out.println("Finished!");
			dos.close();
			targetPeer.fs.close();
		} catch (Exception e) {}
	}
}
