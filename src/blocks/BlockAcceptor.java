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
	}
	
	public void run() {
		try {
			fsLatch = new CountDownLatch(1);
			FileListener ssm = new FileListener(fsLatch);
			Thread serverSocketMakerThread = new Thread(ssm);
			serverSocketMakerThread.setName("Server Socket Maker Thread");
			serverSocketMakerThread.start();
			fsLatch.await();
			System.out.println("File server socket connected with incoming");
			Socket newFS = ssm.getRes();
			peer.setFS(newFS);
			
			DataInputStream dis = new DataInputStream(peer.fs.getInputStream());
			FileOutputStream fos = new FileOutputStream(Utils.defineAppDataDir() 
														+ "/" 
														+ Utils.base64(forFile) 
														+ "/" + blockName);
			byte[] buffer = new byte[(int) Core.chunkSize];
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
