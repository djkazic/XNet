package net;
import java.io.DataInputStream;
import java.io.File;

import blocks.BlockAcceptor;
import blocks.BlockSender;
import main.Core;
import main.Utils;
import peer.Peer;

public class ListenerThread implements Runnable {
	public Peer peer;
	public DataInputStream dis;
	public BlockAcceptor fa;
	
	public ListenerThread(Peer peer, DataInputStream dis) {
		Thread.currentThread().setName("Peer Listener");
		this.peer = peer;
		this.dis = dis;
	}
	
	/**
	 * Processes, in a loop, incoming bytes from the DataInputStream
	 */
	public void run() {
		byte currentFocus;
		while(true) {
			try {
				/** === BLOCK === **/
				currentFocus = dis.readByte();
				if(currentFocus == 0x14) {
					//Got request: disconnect
					peer.disconnect();
				}
				/** === BLOCK === **/
				if(currentFocus == 0x03) {
					//Got request: block list
					String receivedQuery = Utils.readString(dis);
					peer.st.sendBlockList(receivedQuery);
				}
				if(currentFocus == 0x04) {
					//Got data: base64 name / block list
					String preString = Utils.readString(dis);
					if(preString.equals("")) {
						Core.mainWindow.out("Sorry, no files were found for your query.");
					} else {
						String finString = Utils.decrypt(preString);
						Utils.parse(peer, finString);
					}
				}
				/** === BLOCK === **/
				if(currentFocus == 0x05) {
					//Got request: specific block
					/**
					 * base64name
					 * (/)
					 * blockName
					 */
					String allData = Utils.readString(dis);
					String[] split = allData.split("/");
					if(Utils.findBlock(split[0], split[1]) != null) {
						File foundBlock = Utils.findBlock(split[0], split[1]);
						peer.dos.write(0x06);
						peer.dos.flush();
						Utils.writeString(split[0] + "/" + foundBlock.getName(), peer.dos);
						peer.dos.flush();
						peer.dos.writeLong(foundBlock.length());
						peer.dos.flush();
					}
					//TODO: shift away from listenerthread and use blockfilemanager
					//BlockSender fs = new BlockSender(peer, fileSum);
					//(new Thread(fs)).start();
				}
				if(currentFocus == 0x06) {
					//Got data: specific block
					String allData = Utils.readString(dis);
					String[] split = allData.split("/");
					String forFile = split[0];
					String blockName = split[1];
					int filesize = (int) dis.readLong();
					//TODO: shift waay from listenerthread and use blockfilemanager
					//fa = new BlockAcceptor(peer, inputFileName, filesize);
					//(new Thread(fa)).start();
				}
				/** === BLOCK === **/
				if(currentFocus == 0x07) {
					//Got request: HWID
					peer.st.sendHWID();
				}
				if(currentFocus == 0x08) {
					//Got data: HWID
					String incomingHWID = Utils.readString(dis);
					peer.hwid = incomingHWID;
					peer.hwidLatch.countDown();
				}
			} catch (Exception e) {
				e.printStackTrace();
				peer.disconnect(); 
				return;
			}
		} 
	}
}