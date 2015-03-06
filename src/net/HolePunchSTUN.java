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
		this.localPort = localPort;
		stunServerAddress = new StunAddress(stunServer, stunServerPort);
		try {
			Logger root = Logger.getLogger("");
			root.removeHandler(root.getHandlers()[0]);
		} catch (Exception e) {
			Utils.print(this, "Problem detected removing logger");
		}
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
		Utils.print(this, "Identified: " + report.getNatType());
		publicIP = stunLocalAddress.getSocketAddress().getAddress().getHostAddress();
		publicPort = stunLocalAddress.getPort();
	}

	public String getPublicIP() {
		return publicIP;
	}

	public int getPublicPort() {
		return publicPort;
	}
}
