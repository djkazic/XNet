package net;
import java.io.DataInputStream;
import java.io.File;
import java.util.ArrayList;

import blocks.BlockSender;
import blocks.BlockedFile;
import blocks.BlockedFileDL;
import main.Core;
import main.Utils;
import peer.Peer;

public class ListenerThread implements Runnable {
	public Peer peer;
	public DataInputStream dis;
	public SocketWaiter fl;
	
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
				if(currentFocus == 0x00) {
					Utils.print(this, "Pinged!");
				}
				if(currentFocus == 0x14) {
					//Got request: disconnect
					peer.disconnect();
				}
				/** === BLOCK === **/
				if(currentFocus == 0x03) {
					//Got request: block list
					String receivedQuery = Utils.readString(dis);
					if(receivedQuery.length() >= 4) {
						peer.st.sendBlockList(receivedQuery);
					}
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
					File blockSrc = Utils.findFile(Utils.debase64(baseName));
					//If so, serve it up, because we have all blocks!
					if(blockSrc != null) {
						//Get the BlockedFile based on name
						BlockedFile rightBf = Utils.getBlockedFileByName(Utils.debase64(baseName));
						int blockPos = -1;
						if(rightBf != null) {
							blockPos = rightBf.getBlockNumber(blockName);
						}
						if(blockPos != -1) {
							//This means that we have a valid blockPosition
							//BlockSender will send the rest of the response code
							peer.dos.write(0x06);
							peer.dos.flush();
							Utils.writeString(baseName + "/" + blockName, peer.dos);
							peer.dos.flush();
							BlockSender bs = new BlockSender(peer, blockSrc, blockPos, -1);
							(new Thread(bs)).start();
						} else {
							Utils.print(this, "Request denied -- error, invalid blockPos");
						}
						//TODO: implement sending chunks
					}
					//Check to see if we have this block in AppData
					/**
					if(Utils.findBlock(baseName, blockName) != null) {
						//split[0] is baseFileName
						//split[1] is block hash name
						Utils.print(this, "Request approved for block " + blockName + " from " + baseName);
						File foundBlock = Utils.findBlock(baseName, blockName);
						peer.dos.write(0x06);
						peer.dos.flush();
						Utils.writeString(baseName + "/" + foundBlock.getName(), peer.dos);
						peer.dos.flush();
						BlockSender bs = new BlockSender(peer, foundBlock, -1, (int) foundBlock.length());
						(new Thread(bs)).start();
					}
					**/
				}
				if(currentFocus == 0x06) {
					//Got response data: specific block
					/**
					 * base64name
					 * (/)
					 * blockName 
					 * 
					 * fileSize
					 */
					System.out.println("Response received for query!");
					String allData = Utils.readString(dis);
					String[] split = allData.split("/");
					int fileSize = dis.readInt();
					String forFile = split[0];
					String blockName = split[1];
					//Communicate to the right BlockAcceptor that this is a chunk for it
					//TODO: fix null issue
					BlockedFileDL bfdlTest = Utils.getBlockedFileDLForBlock(blockName);
					if(bfdlTest != null) {
						System.out.println("Making FileListener");
						fl = new SocketWaiter(forFile, blockName, fileSize);
						(new Thread(fl)).start();
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
				if(currentFocus == 0x09) {
					//Got request: peerList
					if(Core.peerList.size() > 0) {
						ArrayList<String> basePeers = new ArrayList<String> ();
						if(Core.peerList.size() > 1) {
							for(Peer peer : Core.peerList) {
								if(peer != this.peer && peer.connected) {
									String peerAddr = peer.ps.getRemoteSocketAddress().toString();
									peerAddr = peerAddr.substring(1, peerAddr.length() - 6);
									basePeers.add(peerAddr);
								}
							}
						} else {
							Utils.print(this, "Peerlist requested, but only one is requester");
						}
						if(basePeers.size() > 0) {
							peer.st.sendPeers(basePeers);
						}
					}
				}
				if(currentFocus == 0x10) {
					//Got data: peerList
					String incomingAll = Utils.readString(dis);
					String[] split = incomingAll.split("/");
					for(String peer : split) {
						Core.potentialPeers.add(peer);
					}
				}
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
				peer.disconnect(); 
				return;
			}
		} 
	}
}