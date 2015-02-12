import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Peer implements Runnable {
	public boolean connected;
	public ListenerThread lt;
	public SenderThread st;
	public DataOutputStream dos;
	public DataInputStream dis;
	public Socket ps;
	public int version;
	public long lastPing;
	
	public Peer(Socket ps) {
		this.ps = ps;
		version = -1;
		lastPing = 0;
	}
	
	public void run() {
		try {
			dos = new DataOutputStream(ps.getOutputStream());
			dis = new DataInputStream(ps.getInputStream());
			Core.peerList.add(this);
			ps.setSoTimeout(3500);
			dos.write(0x00);
			dos.flush();
			connected = true;
			lt = new ListenerThread(this, dis);
			(new Thread(lt)).start();
			st = new SenderThread(this, dos);
			(new Thread(st)).start();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public void disconnect() {
		try { dos.write(0x13); } catch (Exception e) {  }
		try { dos.flush(); } catch (Exception e) {  }
		try { dos.close(); } catch (Exception e) {  }
		try { dis.close(); } catch (Exception e) {  }
		try { ps.close(); } catch (Exception e) {  }
		connected = false;
	}
}