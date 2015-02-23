package blocks;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;

import main.Utils;
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

	public BlockAcceptor(Peer peer, String forFile, String blockName, int fileSize) {
		this.peer = peer;
		this.forFile = forFile;
		this.blockName = blockName;
		this.fileSize = fileSize;
	}
	
	public void run() {
		try {
			System.out.println("File server socket connected");
			
			DataInputStream dis = new DataInputStream(peer.fs.getInputStream());
			File pre = new File(Utils.defineAppDataDir() 
														+ "/" 
														+ (forFile));
			if(!pre.exists()) {
				pre.mkdir();
			}
			File post = new File(Utils.defineAppDataDir() 
														+ "/" 
														+ (forFile) //(already base64'd)
														+ "/" + blockName);
			FileOutputStream fos = new FileOutputStream(post, false);
			byte[] buffer = new byte[4096];
			int read = 0;
			int remaining = fileSize;
			while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				remaining -= read;
				fos.write(buffer, 0, read);
			}
			fos.close();
			dis.close();
			Utils.print(this, "Got block successfully");
			//Find BlockedFile forFile and logBlock
			String plain = Utils.debase64(forFile);
			Utils.getBlockedFileByName(plain).logBlock(blockName);
			peer.fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
