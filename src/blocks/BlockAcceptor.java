package blocks;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import main.Core;
import main.Utils;
import net.FileListener;
import peer.Peer;

/**
 * Accepts blocks confirmed to be needed
 * @author caik
 *
 */
public class BlockAcceptor implements Runnable {
	
	private Peer peer;
	private String forFile;
	private String blockName;
	private int fileSize;
	private CountDownLatch fsLatch;

	public BlockAcceptor(Peer peer, String forFile, String blockName, int fileSize) {
		this.peer = peer;
		this.forFile = forFile;
		this.blockName = blockName;
		this.fileSize = fileSize;
		System.out.println("BA Created: fileSize: " + fileSize);
	}
	
	public void run() {
		try {
			FileListener ssm;
			fsLatch = new CountDownLatch(1);
			ssm = new FileListener(fsLatch);
			Thread serverSocketMakerThread = new Thread(ssm);
			serverSocketMakerThread.setName("Server Socket Maker Thread");
			serverSocketMakerThread.start();
			fsLatch.await();

			System.out.println("File server socket generated");
			Socket newFS = ssm.getRes();
			peer.setFS(newFS);
			
			DataInputStream dis = new DataInputStream(peer.fs.getInputStream());
			FileOutputStream fos = new FileOutputStream(Utils.defineAppDataDir() 
														+ "/" 
														+ (forFile) //(already base64'd)
														+ "/" + blockName, false);
			byte[] buffer = new byte[4096];
			int read = 0;
			int remaining = fileSize;
			while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				remaining -= read;
				fos.write(buffer, 0, read);
			}
			fos.close();
			dis.close();
			peer.fs.close();
			Core.mainWindow.out("File transfer of block " + blockName + " for " + forFile + " complete.");
			Utils.print(this, "Got block successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
