package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import peer.Peer;
import blocks.BlockedFile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
		if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
			directory += "\\XNet";
		} else { 
			directory += "/Documents/XNet";
		}
		return directory;
	}
	
	public static String defineAppDataDir() {
		String workingDirectory;
		if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
		    workingDirectory = System.getenv("AppData") + "\\XNet\\";
		} else {
		    workingDirectory = System.getProperty("user.home");
		    workingDirectory += "/Library/Application Support/XNet/";
		}
		return workingDirectory;
	}
	
	public static boolean initAppDataDir(String basename) {
		File workingDirectoryFile = new File(defineAppDataDir() + basename);
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
	}
	
	public static File findBlock(String baseForFile, String block) {
		String decrypted = Utils.debase64(baseForFile);
		File directory = new File(defineDir() + "/" + decrypted);
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
	
	/**
	 * Goes through directory and creates BlockedFile object for each file
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void blockifyDir() throws NoSuchAlgorithmException, IOException {
		File folder = new File(defineDir());
		File[] listOfFiles = folder.listFiles();
		for(int i=0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].isFile()) {
				Core.blockDex.add(new BlockedFile(listOfFiles[i].getAbsolutePath()));
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
		Gson gson = new Gson();
		String[] pairSplit = str.split(";");
		//Also copying into a HashMap for Core
		for(int i=0; i < Core.peerList.size(); i++) {
			String[] slashSplit = pairSplit[i].split("/");
			//Separates the base64 name from the serialized arraylist
			Type type = new TypeToken<ArrayList<String>> () {}.getType();
			ArrayList<String> blockList = gson.fromJson(slashSplit[1], type);
			Core.index.put(Core.peerList.get(i), blockList);
			Core.mainWindow.tableModel.addRow(new String[]{debase64(slashSplit[0]), blockList.toString()});
		}
	}
	
	//HWID utils
	public static String getHWID() {
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
			}
			return base64(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void print(Object sourceClass, String msg) {
		System.out.println("[" + sourceClass.getClass().getName() + "]: " + msg);
	}
}