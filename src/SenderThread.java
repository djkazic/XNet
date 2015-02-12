import java.io.DataOutputStream;

public class SenderThread implements Runnable {
	public Peer peer;
	public DataOutputStream dos;
	
	public SenderThread(Peer peer, DataOutputStream dos) {
		this.peer = peer;
		this.dos = dos;
	}

	public void run() {
		while(peer.connected) {
			try {
				if(System.currentTimeMillis() - peer.lastPing > 1000L) {
					dos.write(0x00);
					dos.flush();
					peer.lastPing = System.currentTimeMillis();
				}
				//Sleep
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) { }
		}	
	}
}