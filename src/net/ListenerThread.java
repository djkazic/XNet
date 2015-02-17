package net;
import java.io.DataInputStream;
import java.io.File;

import blocks.BlockAcceptor;
import blocks.BlockSender;
import blocks.BlockedFileDL;
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
					 * (/)
					 * blockPos
					 */
					String allData = Utils.readString(dis);
					String[] split = allData.split("/");
					//Check to see if we have this complete file to serve blocks from
					String baseName = split[0];
					String blockName = split[1];
					int blockPos = Integer.parseInt(split[2]);
					File blockSrc = Utils.findFile(Utils.debase64(baseName));
					//If so, serve it up! With block position!
					if(blockSrc != null) {
						Utils.print(this, "Request approved, full file detected");
						BlockSender bs = new BlockSender(peer, blockSrc, blockPos, true);
						(new Thread(bs)).start();
					}
					//Check to see if we have this block in AppData
					/**
					if(Utils.findBlock(split[0], split[1]) != null) {
						//split[0] is baseFileName
						//split[1] is block hash name
						Utils.print(this, "Request approved for block " + split[1] + " from " + split[0]);
						File foundBlock = Utils.findBlock(split[0], split[1]);
						peer.dos.write(0x06);
						peer.dos.flush();
						Utils.writeString(split[0] + "/" + foundBlock.getName(), peer.dos);
						peer.dos.flush();
						peer.dos.writeLong(foundBlock.length());
						peer.dos.flush();
						BlockSender bs = new BlockSender(peer, foundBlock);
						(new Thread(bs)).start();
					}
					**/
				}
				if(currentFocus == 0x06) {
					//Got response data: specific block
					String allData = Utils.readString(dis);
					String[] split = allData.split("/");
					String forFile = split[0];
					String blockName = split[1];
					int filesize = (int) dis.readLong();
					//Communicate to the right BlockAcceptor that this is a chunk for it
					BlockedFileDL bfdlTest = Utils.getBlockedFileDLForBlock(blockName);
					if(bfdlTest != null) {
						ba = new BlockAcceptor(peer, forFile, blockName, filesize);
						(new Thread(ba)).start();
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