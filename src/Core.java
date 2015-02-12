import java.util.ArrayList;

public class Core {
	
	public static ArrayList <Peer> peerList;
	
	public static void main(String[] args) {
		peerList = new ArrayList <Peer>();
		
		boolean debugServer = false;
	
		if(debugServer) {
			GlobalListener gl = new GlobalListener();
			(new Thread(gl)).start();
		} else {
			PeerSeeker pst = new PeerSeeker();
			(new Thread(pst)).start();
		}
	}
}
