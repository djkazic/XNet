package net;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ServerSocketMaker implements Runnable {
	
	private CountDownLatch fsLatch;
	private Socket fsSocket;
	
	public ServerSocketMaker(CountDownLatch incoming) {
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
