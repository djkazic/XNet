package net;
import java.io.DataOutputStream;

import main.Core;
import main.Utils;
import peer.Peer;

public class SenderThread implements Runnable {
	public Peer peer;
	public DataOutputStream dos;
	
	private boolean disconnect = false;
	
	private boolean requestNameList = false;
	private boolean sendNameList = false;
	private boolean requestTransfer = false;
	private boolean requestHWID = false;
	private boolean sendHWID = false;
	
	private String sendQuery = "";
	private String receivedQuery = "";
	private String requestedFile = "";
	
	public SenderThread(Peer peer, DataOutputStream dos) {
		Thread.currentThread().setName("Peer Sender ");
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
				
				} else if(requestHWID) {
					//Send request: HWID
					dos.write(0x07);
					dos.flush();
					requestHWID = false;
				} else if(sendHWID) {
					//Send data: HWID
					dos.write(0x08);
					dos.flush();
					Utils.writeString(Core.hwid, dos);
					dos.flush();
					sendHWID = false;
				} else if(disconnect) {
					dos.write(0x14);
					dos.flush();
					disconnect = false;
				}
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) { }
		}	
	}
	
	public void disconnect() {
		disconnect = true;
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
	
	public void requestHWID() {
		requestHWID = true;
	}
	
	public void sendHWID() {
		sendHWID = true;
	}
}