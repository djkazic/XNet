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
	private boolean sendBlockList = false;
	private boolean requestTransfer = false;
	private boolean requestHWID = false;
	private boolean sendHWID = false;
	
	private String sendQuery = "";
	private String receivedQuery = "";
	private String requestedFile = "";
	
	public SenderThread(Peer peer, DataOutputStream dos) {
		Thread.currentThread().setName("Peer Sender");
		this.peer = peer;
		this.dos = dos;
	}
	
	/**
	 * Processes, in a loop pending outgoing data via the DataOutputStream
	 */
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
				} else if(sendBlockList) {
					//Send data: serialized ArrayList<Block>
					dos.write(0x04);
					dos.flush();
					String finRes = Utils.listDirSearch(receivedQuery);
					Utils.writeString(finRes, dos);
					dos.flush();
					receivedQuery = "";
					sendBlockList = false;
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
	
	/**
	 * Sends a disconnect packet to the parent peer
	 */
	public void disconnect() {
		disconnect = true;
	}
	
	/**
	 * Sends a request to the parent peer for a file list
	 * @param str: keyword being sent
	 */
	public void requestNameList(String str) {
		sendQuery = str;
		requestNameList = true;
	}
	
	/**
	 * Sends serialized ArrayList of blocks for keyword
	 * @param str: keyword received from request
	 */
	public void sendBlockList(String str) {
		receivedQuery = str; 
		sendBlockList = true;
	}
	
	/**
	 * Sends a request to the parent peer for file transfer
	 * @param str: md5sum of wanted file
	 */
	public void requestTransfer(String str) {
		requestedFile = str;
		requestTransfer = true;
	}
	
	/**
	 * Sends a request for the parent peer's HWID
	 */
	public void requestHWID() {
		requestHWID = true;
	}
	
	/**
	 * Sends HWID data to the parent peer
	 */
	public void sendHWID() {
		sendHWID = true;
	}
}