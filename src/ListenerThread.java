import java.io.DataInputStream;

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
				if(currentFocus == 0x00) {
					System.out.println("Received ping!");
				}
				//if(currentFocus == 0x13) {
				//	peer.disconnect();
				//}
				if(currentFocus == 0x01) {
					//Receiving version
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