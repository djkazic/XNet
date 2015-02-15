package net.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import net.ServerSocketMaker;
import main.Utils;
import peer.Peer;

//Pumps the file through to peer
public class FileSender implements Runnable {

	private Peer targetPeer;
	private File sending;
	private CountDownLatch socketDone;
	
	public FileSender(Peer peer, String file) {
		this.targetPeer = peer;
		sending = Utils.findBySum(file);
	}

	public void run() {
		try {
			socketDone = new CountDownLatch(1);
			targetPeer.createFS(socketDone);
			socketDone.await();
			System.out.println("Outgoing file socket connected");
			DataOutputStream dos = new DataOutputStream(targetPeer.fs.getOutputStream());
			FileInputStream fis = new FileInputStream(sending);
			byte[] buffer = new byte[4096];
			while (fis.read(buffer) > 0) {
				dos.write(buffer);
			}
			dos.write(0x15);
			dos.flush();
			fis.close();
			dos.close();
			targetPeer.fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
