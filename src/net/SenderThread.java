package net;
import java.io.DataOutputStream;

import main.Utils;
import peer.Peer;

public class SenderThread implements Runnable {
	public Peer peer;
	public DataOutputStream dos;
	
	private boolean requestNameList = false;
	private boolean sendNameList = false;
	private boolean requestTransfer = false;
	
	private String sendQuery = "";
	private String receivedQuery = "";
	private String requestedFile = "";
	
	public SenderThread(Peer peer, DataOutputStream dos) {
		this.peer = peer;
		this.dos = dos;
	}

	public void run() {
		while(peer.connected) {
			try {
				if(System.currentTimeMillis() - peer.lastPing > 1000L) {
					dos.write(0x00);
					dos.flush();
					peer.lastPing = System.currentTimeMillis();
				}
				if(requestNameList) {
					//Send request: name list
					dos.write(0x03);
					dos.flush();
					Utils.writeString(sendQuery, dos);
					dos.flush();
					sendQuery = "";
					requestNameList = false;
				} else if(sendNameList) {
					//Send data: base64 name list
					dos.write(0x04);
					dos.flush();
					String finRes = Utils.listDirSearch(receivedQuery);
					Utils.writeString(finRes, dos);
					dos.flush();
					receivedQuery = "";
					sendNameList = false;
				} else if(requestTransfer) {
					//Send request: file transfer
					dos.write(0x05);
					dos.flush();
					Utils.writeString(requestedFile, dos);
					dos.flush();
					requestedFile = "";
					requestTransfer = false;
					
				//=============== 0x06 is handled in ListenerThread ===============\\
				//======================= DO NOT DEFINE ===========================\\
				
				}
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) { }
		}	
	}
	
	public void requestNameList(String str) {
		sendQuery = str;
		requestNameList = true;
	}
	
	public void sendNameList(String str) {
		receivedQuery = str; 
		sendNameList = true;
	}
	
	public void requestTransfer(String str) {
		requestedFile = str;
		requestTransfer = true;
	}
}