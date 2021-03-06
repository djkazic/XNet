package net;
import java.io.DataOutputStream;
import java.util.ArrayList;

import main.Settings;
import main.Utils;
import peer.Peer;

public class SenderThread implements Runnable {
	public Peer peer;
	public DataOutputStream dos;
	
	private boolean disconnect = false;
	
	private boolean requestBlockList = false;
	private boolean sendBlockList = false;
	private boolean requestBlock = false;
	private boolean requestHWID = false;
	private boolean sendHWID = false;
	private boolean requestPeers = false;
	private boolean sendPeers = false;
	
	private String sendQuery = "";
	private String receivedQuery = "";
	private String requestedBlock = "";
	private String requestedBlockParent = "";
	private String peerList = "";
	
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
				if(requestBlockList) {
					//Send request: name list
					dos.write(0x03);
					dos.flush();
					Utils.writeString(sendQuery, dos);
					dos.flush();
					sendQuery = "";
					requestBlockList = false;
				} else if(sendBlockList) {
					//Send data: serialized ArrayList<Block>
					dos.write(0x04);
					dos.flush();
					String finRes = Utils.listDirSearch(receivedQuery);
					Utils.writeString(finRes, dos);
					dos.flush();
					receivedQuery = "";
					sendBlockList = false;
				} else if(requestBlock) {
					//Send request: block transfer
					dos.write(0x05);
					dos.flush();
					Utils.writeString(Utils.base64(requestedBlockParent) + "/" + requestedBlock, dos);
					dos.flush();
					requestedBlockParent = "";
					requestedBlock = "";
					requestBlock = false;
				} else if(requestHWID) {
					//Send request: HWID
					dos.write(0x07);
					dos.flush();
					requestHWID = false;
				} else if(sendHWID) {
					//Send data: HWID
					dos.write(0x08);
					dos.flush();
					Utils.writeString(Settings.hwid, dos);
					dos.flush();
					sendHWID = false;
				} else if(requestPeers) {
					//Send request: peerList
					dos.write(0x09);
					dos.flush();
					requestPeers = false;
				} else if(sendPeers) {
					//Send data: peerList
					dos.write(0x10);
					dos.flush();
					Utils.writeString(peerList, dos);
					dos.flush();
					sendPeers = false;
				} else if(disconnect) {
					dos.write(0x14);
					dos.flush();
					disconnect = false;
				}
				try {
					Thread.sleep(15);
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
	 * Sends a request to the parent peer for a block list
	 * @param query: keyword being sent
	 */
	public void requestBlockList(String query) {
		sendQuery = query;
		requestBlockList = true;
	}
	
	/**
	 * Sends serialized ArrayList of blocks for keyword
	 * @param wanted: keyword received from request
	 */
	public void sendBlockList(String wanted) {
		receivedQuery = wanted; 
		sendBlockList = true;
	}
	
	/**
	 * Sends request for specific block
	 * @param forFile: block's parent BlockedFile
	 * @param block: block name
	 */
	public void requestBlock(String forFile, String block) {
		requestedBlockParent = forFile;
		requestedBlock = block;
		requestBlock = true;
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
	
	public void requestPeers() {
		requestPeers = true;
	}
	
	public void sendPeers(ArrayList<String> peerList) {
		for(String str : peerList) {
			this.peerList += str + "/";
		}
		if(this.peerList.length() > 0) {
			this.peerList = this.peerList.substring(0, this.peerList.length() - 1);
		}
		sendPeers = true;
	}
}