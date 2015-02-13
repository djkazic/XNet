package peer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PeerSeeker implements Runnable {
	
	public boolean found = false;
	
	public PeerSeeker() {
		System.out.println("INITIALIZING PS");
	}
	
	public void run() {
		while(!found) {
		//TODO: change found variable to "done iterating"
			System.out.println("Attempting hardcode connect");
			Socket peerSocket = new Socket();
			InetSocketAddress peerAddr = new InetSocketAddress("127.0.0.1", 26606);
			try {
				peerSocket.connect(peerAddr);
				System.out.println("Creating peer [out]");
				(new Thread(new Peer(peerSocket))).start();
				System.out.println("Established connection");
				found = true;
			} catch (IOException e) {}
		}
	}
}