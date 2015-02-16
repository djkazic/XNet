package peer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import main.Utils;

public class DiscoveryServer implements Runnable {
	
	private DatagramSocket discoverySocket;
	
	public DiscoveryServer() {
		Thread.currentThread().setName("PeerDiscovery");
	}
	
	public void run() {
		try {
			discoverySocket = new DatagramSocket(26605, InetAddress.getByName("0.0.0.0"));
			discoverySocket.setBroadcast(true);
			while(true) {
				//Receive a packet
				Utils.print(this, "Received potential broadcast packet");
		        byte[] recvBuf = new byte[15000];
		        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
		        discoverySocket.receive(packet);
		        //Check packet contents
		        String message = new String(packet.getData()).trim();
		        if(message.equals("DISCOVER_XNET_REQUEST")) {
		        	byte[] sendData = "DISCOVER_XNET_RESPONSE".getBytes();
			        //Respond
			        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
			        discoverySocket.send(sendPacket);
		        }
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}