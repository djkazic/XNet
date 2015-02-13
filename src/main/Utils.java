package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

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
	
	public static String listDir(String str) {
		String file;
		String totFiles = "";
		File folder = new File(defineDir());
		File[] listOfFiles = folder.listFiles();
		for(int i=0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].isFile()) {
				file = listOfFiles[i].getName();
				if(file.toLowerCase().contains(str.toLowerCase())) {
					totFiles += file + "/";
				}
			}
		}
		if(totFiles.length() > 0) {
			totFiles = totFiles.substring(0, totFiles.length() - 1);
		}
		return totFiles;
	}
	
	//Encryption utils
	public static String encryptList(String list) {
		String output = "";
		String[] split = list.split("/");
		for(int i=0; i < split.length; i++) {
			output += base64(split[i]) + ":";
		}
		output = output.substring(0, output.length() - 1);
		return output;
	}
	
	public static String base64(String input) {
		return Base64.encode(input.getBytes());
	}
}