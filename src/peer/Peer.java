package peer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import main.Core;
import net.ListenerThread;
import net.SenderThread;

public class Peer implements Runnable, Comparable<Peer> {
	public boolean connected;
	public int inout;
	public ListenerThread lt;
	public SenderThread st;
	public DataOutputStream dos;
	public DataInputStream dis;
	public Socket ps;
	public double version = -1;
	public long lastPing = 0;
	public long ms;
	
	public Peer(Socket ps, Long ms, int inout) {
		Core.peerList.add(this);
		this.ps = ps;
		this.ms = ms;
		this.inout = inout;
		Core.sortPeers();
		Core.mainWindow.updatePeerCount();
	}
	
	public void run() {
		try {
			dos = new DataOutputStream(ps.getOutputStream());
			dis = new DataInputStream(ps.getInputStream());
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
		Core.peerList.remove(this);
		try { dos.write(0x14); } catch (Exception e) {  }
		try { dos.flush(); } catch (Exception e) {  }
		try { dos.close(); } catch (Exception e) {  }
		try { dis.close(); } catch (Exception e) {  }
		try { ps.close(); } catch (Exception e) {  }
		connected = false;
	}
	
	public int compareTo(Peer peer) {
		if(this.ms < peer.ms) {
			return -1;
		} else if(this.ms > peer.ms) {
			return 1;
		}
		return 0;
	}
	
	public String toString() {
		return ps.getInetAddress().getHostAddress() + " @ " + ms + "ms";
	}
}