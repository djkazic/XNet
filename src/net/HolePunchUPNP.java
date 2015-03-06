package net;

import gui.WarningPopup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.InetAddress;

import main.Settings;
import main.Utils;
import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.ActionResponse;

import org.wetorrent.upnp.GatewayDevice;
import org.wetorrent.upnp.GatewayDiscover;
import org.wetorrent.upnp.PortMappingEntry;

public class HolePunchUPNP implements Runnable {
	
	private File holePunchConfig;
	private boolean allDone = false;
	
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
				if(line != null && !line.equals("")) {
					sb.append(line);
				}
				String configStatus = sb.toString();
				if(configStatus.equals("UPNP_ENABLE")) {
					return;
				} else if(configStatus.equals("UPNP_ENABLE")) {
					warnPortForward();
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		tryWeUpnp();
		if(!allDone) {
			trySbbi();
		}
	}
	
	private void tryWeUpnp() {
		GatewayDiscover gd = new GatewayDiscover();
		Utils.print(this, "Searching for UPNP router");
		try {
			gd.discover();
		} catch (Exception e) {
			e.printStackTrace();
		}
		GatewayDevice gdev = gd.getValidGateway();
		if(null != gdev) {
			weupnpMapPort(gdev, 26606, "mainPort");
			weupnpMapPort(gdev, 26607, "fsPort");
		} else {
			Utils.print(this, "No UPNP routers found");
			writeResult(false);
			return;
		}
	}
	
	private void weupnpMapPort(GatewayDevice gdev, int port, String description) {
		InetAddress localAddress = gdev.getLocalAddress();
		Utils.print(this, "Attempting to map port " + port);
		PortMappingEntry pme = new PortMappingEntry();
		Utils.print(this, "Querying if port already mapped");
		try {
			if(gdev.getSpecificPortMappingEntry(port, "TCP", pme)) {
				Utils.print(this, "Port " + port + " already mapped");
				if(Settings.removeMapping) {
					gdev.deletePortMapping(port, "TCP");
					Utils.print(this, "Removed existing mapping");
					writeResult(true);
				}
				return;
			} else {
				if(gdev.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", description)) {
					Utils.print(this, "Port " + port + " successfully mapped");
				} else {
					Utils.print(this, "Could not map port " + port);
					writeResult(false);
					return;
				}
				writeResult(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void trySbbi() {
		Utils.print(this, "WeUPNP failed, falling back to SBBI");
		//Disable logging
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		allDone = true;
		int discoveryTimeout = 10000;
		try {
			InternetGatewayDevice[] IGDs = InternetGatewayDevice.getDevices(discoveryTimeout);
			if(IGDs != null) {
				for(int i=0; i < IGDs.length; i++) {
					InternetGatewayDevice igd = IGDs[i];
					sbbiMapPort(igd, 26606);
					sbbiMapPort(igd, 26607);
				}
			} else {
				Utils.print(this, "Unable to find IGD");
				writeResult(false);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sbbiMapPort(InternetGatewayDevice igd, int port) {
		try {
			String localHostIP = Utils.getLocalIpV4();
			boolean mapped = igd.addPortMapping("XNet", null, port, port, localHostIP, 0, "TCP");
			if(mapped) {
				System.out.println("Port " + port + " successfully mapped");
				ActionResponse resp = igd.getSpecificPortMappingEntry(null, port, "TCP");
				if(resp != null) {
					System.out.println("Port mapping confirmed");
				} else {
					writeResult(false);
					return;
				}
				if(Settings.removeMapping) {
					boolean unmapped = igd.deletePortMapping(null, port, "TCP");
					if(unmapped) {
						System.out.println("Port " + port + " unmapped");
					}
				}
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
				allDone = true;
			} else {
				if(allDone = true) {
					writer.println("UPNP_DISABLE");
					warnPortForward();
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void warnPortForward() {
		Thread warnThread = new Thread(new WarningPopup("Your router does not support UPNP. Please manually port forward port 26606 and 26607."));
		warnThread.start();
	}
}
