package main;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import net.HolePunchSTUN;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import peer.Peer;
import blocks.BlockSender;
import blocks.BlockedFile;
import blocks.BlockedFileDL;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import crypto.MD5;

public class Utils {

	//Net Utils
	public static String readString(DataInputStream par0DataInputStream) throws IOException {
		short word0 = par0DataInputStream.readShort();
		StringBuilder stringbuilder = new StringBuilder();
		for (int i = 0; i < word0; i++) {
			stringbuilder.append(par0DataInputStream.readChar());
		}
		return stringbuilder.toString();
	}

	public static void writeString(String par0Str, DataOutputStream par1DataOutputStream) throws IOException {
		if (par0Str.length() > 32767) {
			throw new IOException("String too long");
		} else {
			par1DataOutputStream.writeShort(par0Str.length());
			par1DataOutputStream.writeChars(par0Str);
			return;
		}
	}

	//File Utils
	public static String defineDir() {
		String directory;
		JFileChooser fr = new JFileChooser();
		FileSystemView fw = fr.getFileSystemView();
		directory = fw.getDefaultDirectory().toString();
		if(isWindows()) {
			directory += "/XNet";
		} else { 
			directory += "/Documents/XNet";
		}
		return directory;
	}

	public static String defineAppDataDir() {
		String workingDirectory;
		if(isWindows()) {
			workingDirectory = System.getenv("AppData") + "/XNet";
		} else {
			workingDirectory = defineDir();
			workingDirectory += "/.cache";
		}
		return workingDirectory;
	}

	public static String defineConfigDir() {
		return defineDir() + "/" + ".config";
	}

	public static boolean initAppDataDir(String plainName) {
		String basename = base64(plainName);
		File workingDirectoryFile = new File(defineAppDataDir() + "/" + basename);
		boolean attempt = false;
		if(!workingDirectoryFile.exists()) {
			try {
				workingDirectoryFile.mkdir();
				attempt = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
		}
		return attempt;
	}

	public static void initDir() {
		File findir = new File(defineDir());
		if(!findir.exists()) {
			System.out.println("Could not find directory, creating");
			boolean attempt = false;
			try {
				findir.mkdir();
				attempt = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if(attempt) {
				System.out.println("Successfully created directory");
			}
		}
		File configDir = new File(defineDir() + "/" + ".config");
		if(!configDir.exists()) {
			System.out.println("Could not find config directory, creating");
			boolean attempt = false;
			try {
				configDir.mkdir();
				attempt = true;
			} catch (SecurityException se) {
				se.printStackTrace();
			}
			if(attempt) {
				System.out.println("Successfully created config directory");
			}
		}
		File appDataGen = new File(defineAppDataDir());
		if(!appDataGen.exists()) {
			System.out.println("Could not find appData directory, creating");
			boolean attempt = false;
			try {
				appDataGen.mkdir();
				attempt = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(attempt) {
				System.out.println("Successfully created appData directory");
			}
		}
	}

	/**
	 * Checks AppData directory to see if this block is had
	 * @param baseForFile
	 * @param block
	 * @return
	 */
	public static File findBlock(String baseForFile, String block) {
		File directory = new File(defineAppDataDir() + "/" + baseForFile);
		if(!directory.exists()) {
			return null;
		}
		File[] listOfFiles = directory.listFiles();
		for(int i=0; i < listOfFiles.length; i++) {
			try {
				if(checksum(listOfFiles[i]).equals(block)) {
					return listOfFiles[i];
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static BlockedFile getBlockedFileByName(String blockedFileName) {
		for(BlockedFile block : Core.blockDex) {
			if(block.getName().equals(blockedFileName)) {
				return block;
			}
		}
		return null;
	}

	/**
	 * Checks for complete file related to block
	 * @param plainName
	 * @return
	 */
	public static File findFile(String plainName) {
		File directory = new File(defineDir() + "/" + plainName);
		if(!directory.exists()) {
			return null;
		} else {
			return directory;
		}
	}

	/**
	 * Goes through directory and creates BlockedFile object for each complete file
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void generateBlockDex() throws NoSuchAlgorithmException, IOException {
		File defaultFolder = new File(defineDir());
		File[] listOfFiles = defaultFolder.listFiles();
		for(int i=0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].isFile()) {
				//Create BlockedFile to represent an existent file
				new BlockedFile(listOfFiles[i]);
			}
		}
		File appData = new File(defineAppDataDir());
		File[] listOfFilesAppData = appData.listFiles();
		for(int i=0; i < listOfFilesAppData.length; i++) {
			if(listOfFilesAppData[i].isDirectory()) {
				//Create BlockedFile to represent incomplete file directory
				//TODO: fix duplicate BlockedFile (wholefile) and BlockedFile (incomplete)
				//new BlockedFile(Utils.debase64(listOfFilesAppData[i].getName()));
			}
		}
	}

	public static String listDirSearch(String str) throws NoSuchAlgorithmException, IOException {
		//TODO: conversion finished
		String file = "";
		for(BlockedFile bf : Core.blockDex) {
			if(bf.relevant(str)) {
				file += bf + ";";
			}
		}
		if(file.length() > 0) {
			file = file.substring(0, file.length() - 1);
		}
		return file;
	}

	public static String checksum(File dataFile) throws NoSuchAlgorithmException, IOException {
		return new String(MD5.asHex(MD5.getHash(dataFile)));
	}

	public static String base64(String input) {
		return Base64.encode(input.getBytes());
	}

	public static String debase64(String base64) {
		String output = "";
		try {
			output = new String(Base64.decode(base64.getBytes()), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	public static String decrypt(String chain) {
		String output = "";
		String[] chunkSplit = chain.split(";");
		for(int i=0; i < chunkSplit.length; i++) {
			String[] fileSumSplit = chunkSplit[i].split("/");
			String base64name = fileSumSplit[0];
			String serializedArrayList = fileSumSplit[1];
			output += debase64(base64name) + "/" + serializedArrayList + ";";
		}
		if(output.length() > 0) {
			output = output.substring(0, output.length() - 1);
		}
		return output;
	}

	//Search tools
	public static void doSearch(String keyword) {
		for(Peer p : Core.peerList) {
			//Send out request to all peers
			p.st.requestBlockList(keyword);
		}		
	}

	public static void parse(Peer thisPeer, String str) {
		//Receives serialized data in form of:
		/**
		 * Base64 filename and arraylist of string toString
		 * Delimeters are / and ;
		 * ex. dDkg=fgfDggN/blocklist
		 */
		ObjectMapper mapper = JsonFactory.create();
		String[] pairSplit = str.split(";");
		//Also copying into a HashMap for Core
		for(int i=0; i < Core.peerList.size(); i++) {
			String[] slashSplit = pairSplit[i].split("/");
			//Separates the base64 name from the serialized arraylist
			@SuppressWarnings("unchecked")
			ArrayList<String> blockList = mapper.readValue(slashSplit[1], ArrayList.class, String.class);
			Core.index.put(slashSplit[0], blockList);
			String fileEstimateStr = "";
			int fileEstimateKb = (int) ((Settings.blockSize * blockList.size()) / 1000);
			if(fileEstimateKb > 1000) {
				int fileEstimateMb = (int) (fileEstimateKb / 1000D);
				fileEstimateStr += fileEstimateMb + "MB";
			} else {
				fileEstimateStr += fileEstimateKb+ "KB";
			}
			Core.mainWindow.addRowToSearchModel(new String[]{(slashSplit[0]), fileEstimateStr});
		}
	}

	//HWID utils
	public static String getHWID() throws SocketException {
		String firstInterfaceFound = null;        
		Map<String,String> addrByNet = new HashMap<> ();
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		while(networkInterfaces.hasMoreElements()){
			NetworkInterface network = networkInterfaces.nextElement();
			byte[] bmac = network.getHardwareAddress();
			if(bmac != null){
				StringBuilder sb = new StringBuilder();
				for(int i=0; i < bmac.length; i++) {
					sb.append(String.format("%02X%s", bmac[i], (i < bmac.length - 1) ? "-" : ""));        
				}
				if(!sb.toString().isEmpty()){
					addrByNet.put(network.getName(), sb.toString());
				}
				if(!sb.toString().isEmpty() && firstInterfaceFound == null){
					firstInterfaceFound = network.getName();
				}
			}
		}
		if(firstInterfaceFound != null){
			return base64(addrByNet.get(firstInterfaceFound));
		}
		return null;
	}

	public static void print(Object sourceClass, String msg) {
		System.out.println("[" + sourceClass.getClass().getName() + "]: " + msg);
	}

	public static BlockedFileDL getBlockedFileDLForBlock(String block) {
		for(BlockedFile bf : Core.blockDex) {
			if(bf.getBlockList().contains(block)) {
				if(bf.getDL() == null) {
					System.out.println("Got block but DL is null");
					//TODO: figure out if this means BF is complete
					return null;
				} else {
					return bf.getDL();
				}
			}
		}
		return null;
	}

	public static BlockedFile getBlockedFileByBlockList(ArrayList<String> blockList) {
		for(BlockedFile bf: Core.blockDex) {
			if(bf.getBlockList().containsAll(blockList) && blockList.containsAll(bf.getBlockList())) {
				return bf;
			}
		}
		return null;
	}

	public static String lafStr = "TxMmVaZjVUFWYodUZI50VkJVNtdlM1U0VapkaSRnRXRmVkt2V";

	/**
	 * Deprecated way of pulling blocks from a full-file
	 * @param original
	 * @param blockPos
	 * @return
	 */
	public static File getTempBlock(File original, int blockPos) {
		File mFile = original;
		try {
			double fileLen = (double) mFile.length();
			double numberOfBlocks = (fileLen / Settings.blockSize);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(mFile));
			int i;
			for(i = 0; i < numberOfBlocks - 1; i++) {
				File temp = File.createTempFile("temp", "block");
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
				for(int currentByte = 0; currentByte < Settings.blockSize; currentByte++) {
					out.write(in.read());
				}
				out.close();
				if(blockPos == i) {
					return temp;
				}
				temp.delete();
			}
			//Process last block separately
			if(fileLen != (Settings.blockSize * i)) {
				File temp = File.createTempFile("temp", "block");
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
				//Read rest
				int b;
				while((b = in.read()) != -1) {
					out.write(b);
				}
				out.close();
				if(blockPos == i) {
					return temp;
				}
				temp.delete();
			}
			in.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String lafStrB = "mUIpkMRdkSUd1dKZ0VhlTbVVnSX50T4dEVvVDMWhmQqZFSGdU";

	public static int getRAFBlock(File sending, int blockPos, BlockSender bs) {
		int res = 0;
		try {
			bs.rafBuffer = new byte[(int) Settings.blockSize];
			RandomAccessFile raf = new RandomAccessFile(sending, "r");
			raf.seek(Settings.blockSize * blockPos); //position of block to send
			res = raf.read(bs.rafBuffer);
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Opens a window to the specified URL
	 * @param uri
	 */
	public static void openLink(URI uri) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) { /* TODO: error handling */ }
		} else { /* TODO: error handling */ }
	}

	public static boolean isWindows() {
		//return false;
		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
	}

	public static boolean checkHWID(String hwid) {
		int hCount = 0;
		for(Peer peer : Core.peerList) {
			if(peer.hwid.equals(hwid)) {
				hCount++;
			}
		}
		if(hCount > 1) {
			return false;
		}
		return true;
	}

	public static void sortPeers() {
		Collections.sort(Core.peerList);
	}

	public static String peersCount() {
		int size = Core.peerList.size();
		if(size == 0) {
			//0 peers
			return "0bars";
		} else if(size <= 2) {
			//1 or 2 peers
			return "1bars";
		} else if(size <= 4) {
			//3 or 4 peers
			return "2bars";
		} else if(size > 4) {
			return "3bars";
		}
		return null;
	}

	public static String peerToolTip() {
		int inCount = 0;
		int outCount = 0;
		for(Peer peer : Core.peerList) {
			if(peer.inout == 1) {
				inCount++;
			} else if(peer.inout == 0) {
				outCount++;
			}
		}
		return "[" + inCount + "|" + outCount + "]";
	}

	public static String multidebase64(int rep, String base) {
		String out = base;
		for(int i=0; i < rep; i++) {
			out = debase64(out);
		}
		return out;
	}

	public static String lafStrC = "=0TP3N2Vkx2VIpkVilmSFRWeJ1GZ0YFbXFTWG1Eaw1";

	public static String reverse(String input) {
		char[] in = input.toCharArray();
		int begin=0;
		int end=in.length-1;
		char temp;
		while(end>begin){
			temp = in[begin];
			in[begin]=in[end];
			in[end] = temp;
			end--;
			begin++;
		}
		return new String(in);
	}

	public static String getExtIp() {
		HolePunchSTUN stun = new HolePunchSTUN("stun.ideasip.com", 3478, 26606);
		try {
			stun.performSTUNLookup();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String pubIp = stun.getPublicIP();
		int pubPort = stun.getPublicPort();
		if(pubIp != null && pubPort != -1) {
			return pubIp + ":" + pubPort;
		}
		return null;
	}

	public static String ipToLong(String ipAddressPort) {
		String[] ipPortSplit = ipAddressPort.split(":");
		String ipAddress = ipPortSplit[0];
		String port = ipPortSplit[1];
		String[] ipAddressInArray = ipAddress.split("\\.");
		long ipResult = 0;
		for(int i = 0; i < ipAddressInArray.length; i++) {
			int power = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			ipResult += ip * Math.pow(256, power);
		}
		return ipResult + "|" + port;
	}

	public static String longToIp(long ip) {
		StringBuilder result = new StringBuilder(15);
		for (int i = 0; i < 4; i++) {
			result.insert(0,Long.toString(ip & 0xff));
			if (i < 3) {
				result.insert(0,'.');
			}
			ip = ip >> 8;
		}
		return result.toString();
	}

	public static boolean isValidIPV4(final String s) {  
		final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
		Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
		return IPV4_PATTERN.matcher(s).matches();
	}
} 