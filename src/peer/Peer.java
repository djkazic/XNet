package peer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

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
	public Socket fs;
	public double version = -1;
	public long lastPing = 0;
	public long ms;
	public String hwid = null;
	public CountDownLatch hwidLatch;
	
	public Peer(Socket ps, Long ms, int inout) {
		Core.peerList.add(this);
		this.ps = ps;
		this.ms = ms;
		this.inout = inout;
		Core.sortPeers();
		Core.mainWindow.updatePeerCount();
		hwidLatch = new CountDownLatch(1);
		//If in debug mode, trigger latch w/ incoming
		if(Core.debugServer && inout == 1) {
			Core.incomingDebugReset();
		}
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
			Thread.sleep(200);
			st.requestHWID();
			hwidLatch.await();
			if(Core.checkHWID(hwid) == false) {
				disconnect();
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public void disconnect() {
		Core.peerList.remove(this);
		Core.mainWindow.updatePeerCount();
		st.disconnect();
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
	
	public void createFS(final CountDownLatch done) {
		//Separate FTP socket
		fs = new Socket();
		Runnable fsSeeker = new Runnable() {
			public void run() {
				boolean whoa = false;
				while(!whoa) {
					try {
						System.out.println("Attempting to connect to peer FS at " + ps.getInetAddress());
						InetSocketAddress fsEndpoint = new InetSocketAddress("127.0.0.1", 26607);
						fs.connect(fsEndpoint);
						whoa = true;
						fs.setSoTimeout(3500);
						Thread.sleep(500);
					} catch (Exception e) {
						//e.printStackTrace();
					}
				}
				System.out.println("Connected to peer FS");
				done.countDown();
			}
		};
		Thread fsSeekerThread = new Thread(fsSeeker);
		fsSeekerThread.setName("FS Seeker");
		fsSeekerThread.start();
	}
	
	public void setFS(Socket soc) {
		fs = soc;
		try {
			fs.setSoTimeout(3500);
		} catch (SocketException e) {}
	}
}