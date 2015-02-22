package net;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

import main.Utils;

import org.wetorrent.upnp.GatewayDevice;
import org.wetorrent.upnp.GatewayDiscover;
import org.wetorrent.upnp.PortMappingEntry;

public class HolePunchUPNP implements Runnable {
	
	private CountDownLatch punchLatch;
	
	public HolePunchUPNP(CountDownLatch punchLatch) {
		this.punchLatch = punchLatch;
	}

	public void run() {
		GatewayDiscover gd = new GatewayDiscover();
		Utils.print(this, "Searching for UPNP router");
		try {
			gd.discover();
		} catch (Exception e) {
			e.printStackTrace();
		}
		GatewayDevice gdev = gd.getValidGateway();
		if(gdev == null) {
			Utils.print(this, "No UPNP routers found");
			punchLatch.countDown();
			return;
		} else {
			mapPort(gdev, 26606, "mainPort");
			mapPort(gdev, 26607, "fsPort");
		}
		punchLatch.countDown();
	}
	
	private void mapPort(GatewayDevice gdev, int port, String description) {
		InetAddress localAddress = gdev.getLocalAddress();
		Utils.print(this, "Local address is " + localAddress);
		Utils.print(this, "Attempting to map port " + port);
		PortMappingEntry pme = new PortMappingEntry();
		Utils.print(this, "Querying if port already mapped");
		try {
			if(gdev.getSpecificPortMappingEntry(port, "TCP", pme)) {
				Utils.print(this, "Port already mapped!");
				return;
			} else {
				Utils.print(this, "Sending port mapping request");
				if(gdev.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", description)) {
					Utils.print(this, "Successfully mapped port!");
				} else {
					Utils.print(this, "Could not map port");
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
