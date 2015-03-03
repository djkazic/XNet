package peer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import main.Core;
import main.Utils;

public class DiscoveryThread implements Runnable {

	private DatagramSocket searchSocket;

	public void run() {
		try {
			searchSocket = new DatagramSocket();
			searchSocket.setBroadcast(true);

			byte[] sendData = "DISCOVER_XNET_REQUEST".getBytes();

			//Broadcast to 255.255.255.255
			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 26605);
				searchSocket.send(sendPacket);
				Utils.print(this, "Broadcast packet sent to: 255.255.255.255");
			} catch (Exception e) {}

			// Broadcast the message over all the network interfaces
			Enumeration<?> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue; // Don't broadcast to the loopback interface
				}
				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) {
						continue;
					}
					try {
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
						searchSocket.send(sendPacket);
					} catch (Exception e) {}
				}
			}
			//Wait for a response
			byte[] recvBuf = new byte[15000];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
			searchSocket.receive(receivePacket);
			Utils.print(this, "Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

			String message = new String(receivePacket.getData()).trim();
			String ipv4 = Utils.getIpV4();
			if(!receivePacket.getAddress().getHostAddress().contains(ipv4) && message.equals("DISCOVER_XNET_RESPONSE")) {
				String potentialPeer = receivePacket.getAddress().getHostAddress();
				Core.potentialPeers.add(potentialPeer);
				Utils.print(this, "Local peer identified: " + potentialPeer);
			} else if(receivePacket.getAddress().getHostAddress().contains(ipv4)) {
				Utils.print(this, "Local peer discarded, was self");
			}
			searchSocket.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}