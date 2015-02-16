package blocks;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import main.Core;
import net.FileListener;
import peer.Peer;

/**
 * Is used by BlockedFileManager to accept incoming blocks
 * @author Kevin
 *
 */
public class BlockAcceptor implements Runnable {
	
	private Peer peer;
	private BlockedFile file;
	private String filename;
	private int filesize;
	private CountDownLatch fsLatch;
	
	/**
	 * 
	 * @param peer
	 * @param filename: sent in earlier
	 * @param file
	 * @param filesize
	 */
	public BlockAcceptor(Peer peer, String filename, BlockedFile file, int filesize) {
		this.peer = peer;
		this.filename = filename;
		this.file = file;
		this.filesize = filesize;
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
			FileOutputStream fos = new FileOutputStream(file.getDir() + "/" + filename);
			byte[] buffer = new byte[(int) Core.chunkSize];
			int read = 0;
			int remaining = filesize;
			while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				remaining -= read;
				fos.write(buffer, 0, read);
			}
			fos.close();
			dis.close();
			peer.fs.close();
			Core.mainWindow.out("File transfer of " + filename + " complete.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
