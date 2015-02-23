package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import blocks.BlockAcceptor;

public class SocketWaiter implements Runnable {
	
	private Socket fsSocket;
	private String forFile;
	private String blockName;
	private int fileSize;
	private ServerSocket fs;
	
	public SocketWaiter(String forFile, String blockName, int fileSize) {
		this.forFile = forFile;
		this.blockName = blockName;
		this.fileSize = fileSize;
		Thread.currentThread().setName("FileListener");
		try {
			fs = new ServerSocket(26607);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(true) {
			try {
				fsSocket = fs.accept();
				(new Thread(new BlockAcceptor(fsSocket, forFile, blockName, fileSize))).start();
				System.out.println("FileListener spawned BlockAcceptor");
				resetVars();
				//Wait until you get variables again
				while(forFile == null || blockName == null || fileSize == 0) {
					Thread.sleep(1000);
				}
			} catch(Exception e) {}
		}
	}
	
	public void setVars(String forFile, String blockName, int fileSize) {
		this.forFile = forFile;
		this.blockName = blockName;
		this.fileSize = fileSize;
	}
	
	private void resetVars() {
		forFile = null;
		blockName = null;
		fileSize = 0;
	}
}
