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
	public BlockAcceptor ba;
	
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
					//Check to see if we have this block in AppData
					if(Utils.findBlock(split[0], split[1]) != null) {
						Utils.print(this, "Request approved for block " + split[1] + " from " + split[0]);
						File foundBlock = Utils.findBlock(split[0], split[1]);
						peer.dos.write(0x06);
						peer.dos.flush();
						Utils.writeString(split[0] + "/" + foundBlock.getName(), peer.dos);
						peer.dos.flush();
						peer.dos.writeLong(foundBlock.length());
						peer.dos.flush();
						BlockSender fs = new BlockSender(peer, foundBlock);
						//(new Thread(fs)).start();
					} else {
						Utils.print(this, "Request denied for block " + split[1] + " from " + split[0]);
					}
				}
				if(currentFocus == 0x06) {
					//Got response data: specific block
					String allData = Utils.readString(dis);
					String[] split = allData.split("/");
					String forFile = split[0];
					String blockName = split[1];
					int filesize = (int) dis.readLong();
					Utils.print(this, "Got block data! Name = " + split[1] + " for " + split[0]);
					//Somehow communicate to the right BlockAcceptor that this is a chunk for it
					if(Utils.getBlockedFileDLForBlock(forFile, blockName) != null) {
						Utils.print(this, "Confirmed block " + blockName + " is needed");
						ba = new BlockAcceptor(peer, forFile, blockName, filesize);
						//(new Thread(ba)).start();
						Utils.getBlockedFileDLForBlock(forFile, blockName).logBlock(blockName);
					}
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