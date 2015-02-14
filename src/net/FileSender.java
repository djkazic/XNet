package net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import main.Utils;
import peer.Peer;

//Pumps the file through to peer
public class FileSender implements Runnable {

	private Peer peer;
	private File target;
	
	public FileSender(Peer peer, String file) {
		System.out.println("FileSender object created");
		this.peer = peer;
		target = Utils.findBySum(file);
		System.out.println("Target: " + target.getName());
	}

	public void run() {
		System.out.println("FileSender started.");
		try {
			byte[] mybytearray = new byte[(int) target.length()];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(target));
			bis.read(mybytearray, 0, mybytearray.length);
			OutputStream os = peer.ps.getOutputStream();
			os.write(mybytearray, 0, mybytearray.length);
			os.flush();
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("FileSender stopped.");
	}
}
