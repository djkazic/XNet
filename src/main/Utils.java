package main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import peer.Peer;
import blocks.BlockSender;
import blocks.BlockedFile;
import blocks.BlockedFileDL;

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
		    workingDirectory = System.getenv("AppData") + "/XNet";
		} else {
		    workingDirectory = System.getProperty("user.home");
		    workingDirectory += "/Library/Application Support/XNet";
		}
		return workingDirectory;
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
		Gson gson = new Gson();
		String[] pairSplit = str.split(";");
		//Also copying into a HashMap for Core
		for(int i=0; i < Core.peerList.size(); i++) {
			String[] slashSplit = pairSplit[i].split("/");
			//Separates the base64 name from the serialized arraylist
			Type type = new TypeToken<ArrayList<String>> () {}.getType();
			ArrayList<String> blockList = gson.fromJson(slashSplit[1], type);
			Core.index.put(Core.peerList.get(i), blockList);
			Core.mainWindow.tableModel.addRow(new String[]{(slashSplit[0]), blockList.toString()});
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
	
	public static BlockedFile getBlockedFile(ArrayList<String> blockList) {
		for(BlockedFile bf: Core.blockDex) {
			if(bf.getBlockList().containsAll(blockList) && blockList.containsAll(bf.getBlockList())) {
				return bf;
			}
		}
		return null;
	}

	public static BlockedFile getBlockedFile(String baseName) {
		for(BlockedFile bf : Core.blockDex) {
			if(bf.getName().equals(debase64(baseName))) {
				return bf;
			}
		}
		return null;
	}

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
			double numberOfBlocks = (fileLen / Core.chunkSize);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(mFile));
			int i;
			for(i = 0; i < numberOfBlocks - 1; i++) {
				File temp = File.createTempFile("temp", "block");
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
				for(int currentByte = 0; currentByte < Core.chunkSize; currentByte++) {
					out.write(in.read());
				}
				out.close();
				if(blockPos == i) {
					return temp;
				}
				temp.delete();
			}
			//Process last block separately
			if(fileLen != (Core.chunkSize * i)) {
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

	public static int getRAFBlock(File sending, int blockPos, BlockSender bs) {
		int res = 0;
		try {
			bs.rafBuffer = new byte[(int) Core.chunkSize];
			RandomAccessFile raf = new RandomAccessFile(sending, "r");
			raf.seek(Core.chunkSize * blockPos); //position of block to send
			res = raf.read(bs.rafBuffer);
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
}