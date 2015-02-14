package net.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import main.Utils;
import peer.Peer;

//Pumps the file through to peer
public class FileSender implements Runnable {

	private Peer targetPeer;
	private File sending;
	
	public FileSender(Peer peer, String file) {
		this.targetPeer = peer;
		sending = Utils.findBySum(file);
	}

	public void run() {
		try {
			byte[] mybytearray = new byte[(int) sending.length()];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sending));
			bis.read(mybytearray, 0, mybytearray.length);
			OutputStream os = targetPeer.ps.getOutputStream();
			os.write(mybytearray, 0, mybytearray.length);
			os.flush();
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
