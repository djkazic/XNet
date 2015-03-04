package net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import main.Core;
import main.Utils;

public class IRCBootstrap implements Runnable {

	private String channel;
	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;
	private String line;
	
	public IRCBootstrap(String server, String channel, int port) throws Exception {
		this.channel = channel;
		socket = new Socket(server, port);
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		line = null;
	}

	public void run() {
		Utils.print(this, "Initializing IRC bootstrap");
		try {
			connect();
		} catch (IOException e) {e.printStackTrace();}
	}

	private void connect() throws IOException {
		//Get full external IP and filePort
		String extIp = Utils.getExtIp(26606, false);
		String filePort = Utils.getExtIp(26607, true);
		
		//Set nick to long encoded IP
		String nick = "X" + Utils.ipToHex(extIp);
		Utils.print(this, "Set " + nick + " for bootstrap");
		String login = "xagent";

		// Log on to the server.
		writer.write("NICK " + nick + "\r\n");
		writer.write("USER " + login + " 8 * : XNet\r\n");
		writer.flush();

		// Read lines from the server until it tells us we have connected.
		while((line = reader.readLine()) != null) {
			if(line.contains("004")) {
				break;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Join the channel.
		writer.write("JOIN " + channel + "\r\n");
		writer.flush();
		Utils.print(this, "Joined bootstrap channel successfully");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Keep reading lines from the server.
		while((line = reader.readLine()) != null) {
			if(line.startsWith("PING ")) {
				try {
					writer.write("PONG " + line.substring(5) + "\r\n");
					writer.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
				//TODO: add timed /NAMES command
			} else if(line.contains(" 353 " + nick)) {
				int firstIndex = line.indexOf(":");
				String firstIndexOut = line.substring(firstIndex + 1);
				int secondIndex = firstIndexOut.indexOf(":");
				String finalLine = firstIndexOut.substring(secondIndex + 1);
				String[] ipList = finalLine.split(" ");
				for(String str : ipList) {
					if(str.startsWith("@")) {
						str = str.substring(1);
					}
					if(!str.equals(nick) &&  !(attemptDecode(str)).equals("")) {
						String decoded = attemptDecode(str);
						Utils.print(this, "Got peer from IRC: " + decoded);
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Core.potentialPeers.add(decoded);
					}
				}
			} else if(line.contains("@") && line.contains("JOIN " + channel)) {
				String[] splitOne = line.split("!");
				String[] splitTwo = splitOne[0].split(":");
				String encoded = splitTwo[0];
				if(!encoded.equals(nick) && !(attemptDecode(encoded)).equals("")) {
					String decoded = attemptDecode(encoded);
					String ip = decoded.split(":")[0];
					String port = decoded.split(":")[1];
					//Send UDP to punch hole (RECV)
					DatagramSocket punchSocket = new DatagramSocket();
					byte[] sendData = new byte[1024];
					InetAddress iaIp = InetAddress.getByName(ip);
					DatagramPacket punchPacket = new DatagramPacket(sendData, sendData.length, iaIp, Integer.parseInt(port));
					punchSocket.send(punchPacket);
					Utils.print(this, "Sent punchPacket to " + ip);
					punchSocket.close();
					//TODO: If punching packets work for PS
					//Mesage "encoded" and ask for fileSocket port
					//Punch fileSocket after 200 ms to be able to DL files (secondary)
				}
			} //else if PRIVMSG from a peer requesting fileSocket, send filePort
			  //First punch to be able to DL files
			  //System.out.println(line);
		}
	}

	private String attemptDecode(String str) {
		if(str.startsWith("X")) {
			String ip = Utils.hexToIp(str.substring(1));
			int colonIndex = ip.indexOf(":");
			String testIp = ip.substring(0, colonIndex);
			if(Utils.isValidIPV4(testIp)) {
				return ip;
			}
		}
		return "";
	}

	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {}
	}

	public String getChannel() {
		return channel;
	}

	public BufferedWriter getWriter() {
		return writer;
	}
}