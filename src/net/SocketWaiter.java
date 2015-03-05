package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import main.Utils;
import peer.Peer;
import blocks.BlockAcceptor;

public class SocketWaiter implements Runnable {
	
	private Socket fsSocket;
	private Peer peer;
	private String forFile;
	private String blockName;
	private int fileSize;
	private ServerSocket fs;
	
	public SocketWaiter(Peer peer, String forFile, String blockName, int fileSize) {
		this.peer = peer;
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
				peer.fs = fsSocket;
				//Enable TLS
				SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());
				InetSocketAddress remoteAddress = (InetSocketAddress) peer.fs.getRemoteSocketAddress();
				SSLSocket sslSocket = (SSLSocket) (sf.createSocket(peer.fs, remoteAddress.getHostName(), peer.fs.getPort(), true));
				sslSocket.setUseClientMode(false);
				sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
				sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
				sslSocket.startHandshake();
				if(sslSocket.isConnected()) {
					Utils.print(this, "TLS enabled. File transmission [I] secured");
				}
				peer.fs = sslSocket;
				(new Thread(new BlockAcceptor(peer, forFile, blockName, fileSize))).start();
				System.out.println("FileListener spawned BlockAcceptor");
				resetVars();
				//Wait until you get variables again
				while(forFile == null || blockName == null || fileSize == 0) {
					Thread.sleep(500);
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
