package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import peer.Peer;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

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
		if(System.getProperty("os.name").indexOf("mac") >= 0) {
			directory += "\\Documents\\XNet";
		} else { 
			directory += "\\XNet";
		}
		return directory;
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
	
	public static File findBySum(String sum) {
		File directory = new File(defineDir());
		File[] listOfFiles = directory.listFiles();
		for(int i=0; i < listOfFiles.length; i++) {
			try {
				if(checksum(listOfFiles[i]).equals(sum)) {
					return listOfFiles[i];
				}
			} catch (NoSuchAlgorithmException | IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String listDir() throws NoSuchAlgorithmException, IOException {
		String file;
		String totFiles = "";
		File folder = new File(defineDir());
		File[] listOfFiles = folder.listFiles();
		for(int i=0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].isFile()) {
				file = listOfFiles[i].getName();
				totFiles += base64(file) + "/" + checksum(listOfFiles[i]) + ";";
			}
		}
		if(totFiles.length() > 0) {
			totFiles = totFiles.substring(0, totFiles.length() - 1);
		}
		return totFiles;
	}
	
	public static String listDirSearch(String str) throws NoSuchAlgorithmException, IOException {
		String file = "";
		String totFiles = decrypt(Core.md5dex);
		//Split and add only those that match condition
		String[] totSplit = totFiles.split(";");
		for(int i=0; i < totSplit.length; i++) {
			String[] fSplit = totSplit[i].split("/");
			if(fSplit[0].toLowerCase().contains(str.toLowerCase())) {
				file += base64(fSplit[0]) + "/" + fSplit[1] + ";";
			}
		}
		if(file.length() > 0) {
			file = file.substring(0, file.length() - 1);
		}
		return file;
	}

	public static String checksum(File datafile) throws NoSuchAlgorithmException, IOException {
		InputStream fis =  new FileInputStream(datafile);
		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		byte[] bytes = complete.digest();
		String result = "";
		for (int i=0; i < bytes.length; i++) {
			result += Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	
	public static String base64(String input) {
		return Base64.encode(input.getBytes());
	}
	
	public static String decrypt(String chain) {
		String output = "";
		String[] chunkSplit = chain.split(";");
		for(int i=0; i < chunkSplit.length; i++) {
			String[] fileSumSplit = chunkSplit[i].split("/");
			output += debase64(fileSumSplit[0]) + "/" + fileSumSplit[1] + ";";
		}
		if(output.length() > 0) {
			output = output.substring(0, output.length() - 1);
		}
		return output;
	}
	
	public static String debase64(String base64) {
		String output = "";
		try {
			output = new String(Base64.decode(base64.getBytes()), "UTF-8");
		} catch (UnsupportedEncodingException | Base64DecodingException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	//Search tools
	public static void doSearch(String str) {
		for(Peer p : Core.peerList) {
			//Send out request to all peers
			p.st.requestNameList(str);
		}		
	}
	
	public static void parse(Peer thisPeer, String str) {
		//Receives data in form of:
		/**
		 * filename/checksum;filename/checksum etc.
		**/
		//Create ArrayList of just filenames
		String[] pairSplit = str.split(";");
		//Also copying into a HashMap for Core
		for(int i=0; i < Core.peerList.size(); i++) {
			String[] slashSplit = pairSplit[i].split("/");
			Core.fileToHash.add(slashSplit);			
			//Interpret this as a String[] when selected
			Core.index.put(Core.peerList.get(i), slashSplit);
		}
		for(String[] strA : Core.fileToHash) { 
			Core.mainWindow.tableModel.addRow(strA);
		}
	}
}