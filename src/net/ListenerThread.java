package net;
import java.io.DataInputStream;

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
				if(currentFocus == 0x13) {
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
			} catch (Exception e) { 
				peer.disconnect(); 
				System.out.println("Network error: peer disconnection");
				return;
			}
		} 
	}
}