package net;
import java.io.DataInputStream;

import net.io.FileAcceptor;
import net.io.FileSender;
import main.Core;
import main.Utils;
import peer.Peer;

public class ListenerThread implements Runnable {
	public Peer peer;
	public DataInputStream dis;
	
	public ListenerThread(Peer peer, DataInputStream dis) {
		this.peer = peer;
		this.dis = dis;
	}

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
					//Got request: name list
					String receivedQuery = Utils.readString(dis);
					System.out.println("Asked for namelist of " + receivedQuery);
					peer.st.sendNameList(receivedQuery);
				}
				if(currentFocus == 0x04) {
					//Got data: base64 name list
					String preString = Utils.readString(dis);
					if(preString.equals("")) {
						Core.mainWindow.out("Sorry, no items found for your query.");
					} else {
						String finString = Utils.decrypt(preString);
						Utils.parse(peer, finString);
					}
				}
				/** === BLOCK === **/
				if(currentFocus == 0x05) {
					//Got request: transfer file
					String fileSum = Utils.readString(dis);
					peer.dos.write(0x06);
					peer.dos.flush();
					Utils.writeString(Utils.findBySum(fileSum).getName(), peer.dos);
					FileSender fs = new FileSender(peer, fileSum);
					(new Thread(fs)).start();
				}
				if(currentFocus == 0x06) {
					//Got data: transfer file
					String fileName = Utils.readString(dis);
					FileAcceptor fa = new FileAcceptor(peer, fileName);
					(new Thread(fa)).start();
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
				Core.mainWindow.out("Network error: peer disconnection");
				return;
			}
		} 
	}
}