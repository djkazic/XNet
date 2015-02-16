package net;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class FileListener implements Runnable {
	
	private CountDownLatch fsLatch;
	private Socket fsSocket;
	
	public FileListener(CountDownLatch incoming) {
		Thread.currentThread().setName("FileListener");
		fsLatch = incoming;
	}
	
	@SuppressWarnings("resource")
	public void run() {
		ServerSocket fs;
		while(true) {
			try {
				fs = new ServerSocket(26607);
				fsSocket = fs.accept();
				fsLatch.countDown();
			} catch(Exception e) {}
		}
	}
	
	public Socket getRes() {
		return fsSocket;
	}
}
