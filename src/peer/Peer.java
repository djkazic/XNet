package peer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import main.Core;
import net.ListenerThread;
import net.SenderThread;

public class Peer implements Runnable {
	public boolean connected;
	public ListenerThread lt;
	public SenderThread st;
	public DataOutputStream dos;
	public DataInputStream dis;
	public Socket ps;
	public int version = -1;
	public long lastPing = 0;;
	
	public Peer(Socket ps) {
		this.ps = ps;
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
			st.requestVersion();
			//Disconnect from out dated peers
			if(version < Core.version) {
				disconnect();
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	public int getVersion() {
		if(version == -1) {
			st.requestVersion();
		}
		return version;
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