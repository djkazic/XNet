package net;

import java.util.logging.Logger;

import main.Utils;
import net.java.stun4j.client.StunDiscoveryReport;
import net.java.stun4j.StunAddress;
import net.java.stun4j.client.NetworkConfigurationDiscoveryProcess;

public class HolePunchSTUN {

	private int localPort;
	private StunAddress stunServerAddress;
	private StunAddress stunLocalAddress;
	private int publicPort;
	private String publicIP; 

	public HolePunchSTUN(String stunServer, int stunServerPort, int localPort) {
		Logger root = Logger.getLogger("");
		root.removeHandler(root.getHandlers()[0]);
		this.localPort = localPort;
		stunServerAddress = new StunAddress(stunServer, stunServerPort);
	}

	public void performSTUNLookup() throws Exception {
		NetworkConfigurationDiscoveryProcess stunDiscovery = new NetworkConfigurationDiscoveryProcess(new StunAddress(localPort), stunServerAddress);
		stunDiscovery.start();
		StunDiscoveryReport report = stunDiscovery.determineAddress();
		stunDiscovery.shutDown();
		stunLocalAddress = null;
		stunLocalAddress = report.getPublicAddress();
		if (stunLocalAddress == null || stunLocalAddress.getSocketAddress() == null || stunLocalAddress.getSocketAddress().getAddress() == null) {
			Utils.print(this, "STUN encountered an error, IP was null");
		}

		//just get the IP address.. the other info is excessive
		publicIP = stunLocalAddress.getSocketAddress().getAddress().getHostAddress();
		publicPort = stunLocalAddress.getPort();
	}

	public String getPublicIP() {
		return publicIP;
	}

	public int getPublicPort() {
		return publicPort;
	}

	public static void main(String[] args) {
		try {
			String stunServerAddress = new String("stun.ideasip.com");
			int stunServerPort = 3478;

			//this is the port your program wants to be able to use.
			int desiredPort = 26606;

			//create a stun utility tool (makes stun4j a little easier)
			HolePunchSTUN stun = new HolePunchSTUN(stunServerAddress, stunServerPort, desiredPort);

			//talk to the stun server and figure out the NAT information
			stun.performSTUNLookup();

			//now print out the info that users outside the internet can use to connect to you.
			System.out.println("Internet users can connect to my IP address " + stun.getPublicIP() + " and port " + stun.getPublicPort());

		} catch (Exception e) {
			System.out.println("Failed to lookup NAT information via STUN: " + e.getMessage());
		}

	}
}
