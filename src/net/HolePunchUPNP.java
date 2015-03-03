package net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import main.Utils;
import org.wetorrent.upnp.GatewayDevice;
import org.wetorrent.upnp.GatewayDiscover;
import org.wetorrent.upnp.PortMappingEntry;

public class HolePunchUPNP implements Runnable {
	
	private File holePunchConfig;

	public void run() {
		try {
			holePunchConfig = new File(Utils.defineConfigDir() + "/" + "holePunching.dat");
			if(!holePunchConfig.exists()) {
				holePunchConfig.createNewFile();
			} else {
				BufferedReader br = new BufferedReader(new FileReader(holePunchConfig));
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				br.close();
				if(line != null) {
					sb.append(line);
				}
				String configStatus = sb.toString();
				if(configStatus.equals("UPNP_DISABLE") || configStatus.equals("UPNP_ENABLE")) {
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		GatewayDiscover gd = new GatewayDiscover();
		Utils.print(this, "Searching for UPNP router");
		try {
			gd.discover();
		} catch (Exception e) {
			e.printStackTrace();
		}
		GatewayDevice gdev = gd.getValidGateway();
		if(null != gdev) {
			mapPort(gdev, 26606, "mainPort");
			mapPort(gdev, 26607, "fsPort");
		} else {
			Utils.print(this, "No UPNP routers found");
			writeResult(false);
			return;
		}
		
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
				if(Settings.removeMapping) {
					gdev.deletePortMapping(port, "TCP");
				}
				return;
			} else {
				Utils.print(this, "Sending port mapping request");
				if(gdev.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", description)) {
					Utils.print(this, "Successfully mapped port!");
				} else {
					Utils.print(this, "Could not map port");
					writeResult(false);
					return;
				}
				writeResult(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeResult(boolean compatible) {
		try {
			PrintWriter writer = new PrintWriter(holePunchConfig, "UTF-8");
			if(compatible) {
				writer.println("UPNP_ENABLE");
			} else {
				writer.println("UPNP_DISABLE");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
