package net.io;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import main.Core;
import main.Utils;
import net.ServerSocketMaker;
import peer.Peer;

public class FileAcceptor implements Runnable {
	
	private Peer peer;
	private String filename;
	private int filesize;
	private CountDownLatch fsLatch;
	
	public FileAcceptor(Peer peer, String filename, int filesize) {
		this.peer = peer;
		this.filename = filename;
		this.filesize = filesize;
	}
	
	public void run() {
		try {
			fsLatch = new CountDownLatch(1);
			ServerSocketMaker ssm = new ServerSocketMaker(fsLatch);
			Thread serverSocketMakerThread = new Thread(ssm);
			serverSocketMakerThread.setName("Server Socket Maker Thread");
			serverSocketMakerThread.start();
			fsLatch.await();
			System.out.println("FSL connected with incoming");
			Socket newFS = ssm.getRes();
			peer.setFS(newFS);
			
			DataInputStream dis = new DataInputStream(peer.fs.getInputStream());
			FileOutputStream fos = new FileOutputStream(Utils.defineDir() + "\\" + "RECV" + filename);
			byte[] buffer = new byte[4096];
			int read = 0;
			int remaining = filesize;
			while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				remaining -= read;
				fos.write(buffer, 0, read);
			}
			fos.close();
			dis.close();
			Core.mainWindow.out("File transfer of " + filename + " complete.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
