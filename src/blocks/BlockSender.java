package blocks;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;

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

	public BlockSender(Peer peer, File sending) {
		this.targetPeer = peer;
		this.sending = sending;
	}

	public void run() {
		try {
			Utils.print(this, "Sending " + sending.getName());
			socketDone = new CountDownLatch(1);
			targetPeer.createFS(socketDone);
			socketDone.await();
			System.out.println("Outgoing file socket connected");
			DataOutputStream dos = new DataOutputStream(targetPeer.fs.getOutputStream());
			FileInputStream fis = new FileInputStream(sending);
			//Need filesize to be sent just in case block is smaller
			byte[] buffer = new byte[4096];
			while(fis.read(buffer) > 0) {
				dos.write(buffer);
			}
			dos.flush();
			fis.close();
			dos.close();
			targetPeer.fs.close();
		} catch (Exception e) {}
	}
}
