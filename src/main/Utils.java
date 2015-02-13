package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

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
	
	public static String listDir(String str) throws NoSuchAlgorithmException, IOException {
		String file;
		String totFiles = "";
		File folder = new File(defineDir());
		File[] listOfFiles = folder.listFiles();
		for(int i=0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].isFile()) {
				file = listOfFiles[i].getName();
				if(file.toLowerCase().contains(str.toLowerCase())) {
					totFiles += base64(file) + "/" + checksum(defineDir() + "\\" + file) + ";";
				}
			}
		}
		if(totFiles.length() > 0) {
			totFiles = totFiles.substring(0, totFiles.length() - 1);
		}
		return totFiles;
	}
	
	public static String checksum(String datafile) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("SHA1");
		FileInputStream fis = new FileInputStream(datafile);
		byte[] dataBytes = new byte[1024];
		int nread = 0; 
		while ((nread = fis.read(dataBytes)) != -1) {
			md.update(dataBytes, 0, nread);
		};
		byte[] mdbytes = md.digest();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		fis.close();
		return sb.toString();
	}
	
	public static String base64(String input) {
		return Base64.encode(input.getBytes());
	}
}