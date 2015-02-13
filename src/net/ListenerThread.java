package net;
import java.io.DataInputStream;

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
				currentFocus = dis.readByte();
				if(currentFocus == 0x14) {
					//Got request: disconnect
					peer.disconnect();
				}
				if(currentFocus == 0x01) {
					//Got request: version
					peer.st.sendVersion();
				}
				if(currentFocus == 0x02) {
					//Got data: version
					peer.version = dis.readInt();
				}
				if(currentFocus == 0x03) {
					//Got request: name list
					String receivedQuery = Utils.readString(dis);
					peer.st.sendNameList(receivedQuery);
				}
				if(currentFocus == 0x04) {
					//Got data: base64 name list
					System.out.println(Utils.readString(dis));
				}
			} catch (Exception e) {
				e.printStackTrace();
				peer.disconnect(); 
				System.out.println("Network error: peer disconnection");
				return;
			}
		} 
	}
}