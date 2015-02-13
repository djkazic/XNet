package net;
import java.io.DataOutputStream;

import main.Core;
import main.Utils;
import peer.Peer;

public class SenderThread implements Runnable {
	public Peer peer;
	public DataOutputStream dos;
	
	private boolean requestVersion = false;
	private boolean sendVersion = false;
	private boolean requestNameList = false;
	private boolean sendNameList = false;
	private String sendQuery = "";
	private String receivedQuery = "";
	
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
				if(requestVersion) {
					//Send request: version
					dos.write(0x01);
					dos.flush();
					requestVersion = false;
				} else if(sendVersion) {
					//Send data: version
					dos.write(0x02);
					dos.flush();
					dos.writeDouble(Core.version);
					dos.flush();
					sendVersion = false;
				} else if(requestNameList) {
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
				}
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) { }
		}	
	}
	
	public void requestVersion() {
		requestVersion = true;
	}
	
	public void sendVersion() {
		sendVersion = true;
	}
	
	public void requestNameList(String str) {
		sendQuery = str;
		requestNameList = true;
	}
	
	public void sendNameList(String str) {
		receivedQuery = str; 
		sendNameList = true;
	}
}