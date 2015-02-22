package net;

import java.net.ServerSocket;
import java.net.Socket;

import blocks.BlockAcceptor;

public class SocketWaiter implements Runnable {
	
	private Socket fsSocket;
	private String forFile;
	private String blockName;
	private int fileSize;
	
	public SocketWaiter(String forFile, String blockName, int fileSize) {
		this.forFile = forFile;
		this.blockName = blockName;
		this.fileSize = fileSize;
		Thread.currentThread().setName("FileListener");
	}
	
	public void run() {
		ServerSocket fs;
		while(true) {
			try {
				fs = new ServerSocket(26607);
				fsSocket = fs.accept();
				(new Thread(new BlockAcceptor(fsSocket, forFile, blockName, fileSize))).start();
				System.out.println("FileListener terminating");
				fs.close();
				return;
			} catch(Exception e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {}
			}
		}
	}
}
