package blocks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import main.Core;
import main.Utils;
import com.google.gson.Gson;

//Represents files as a file and group of Blocks
public class BlockedFile {

	private String file;
	private ArrayList<String> blockList;
	private ArrayList<String> presentBlocks;
	private Gson gson;

	//First-time constructor
	public BlockedFile(String file, boolean share) {
		this.file = file;
		blockList = new ArrayList<String> ();
		gson = new Gson();
		getBlocks();
	}
	
	public BlockedFile() {
		//This is empty so you can load when you deserialize
	}
	
	public static void main(String[] args) {
		BlockedFile bf = new BlockedFile("C:/install.exe");
		System.out.println(bf);
	}
	
	private void getBlocks() {
		//First time
		File mFile = new File(file);
		try {
			//calculate number of blocks
			double fileLen = (double) mFile.length();
			System.out.println(fileLen);
			double numberOfBlocks = (fileLen / Core.chunkSize);
			System.out.println(numberOfBlocks);
			//Process all blocks but one
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(mFile));
			int i;
			for(i = 0; i < (fileLen/Core.chunkSize) - 1; i++) {
				System.out.println("Regular proc: " + i);
				//make blocks into temp, checksum, then delete
				//File temp = File.createTempFile("temp", "block");
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Users/Kevin/Desktop/installParts/p" + "." + i + ".dat")));
				for (int currentByte = 0; currentByte < Core.chunkSize; currentByte++) {
					out.write(in.read());
				}
				out.close();
				try {
					blockList.add(Utils.checksum(new File("C:/Users/Kevin/Desktop/installParts/p" + "." + i + ".dat")));
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				//temp.delete();
			}
			//Process last block separately
			if(fileLen != Core.chunkSize * i) {
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Users/Kevin/Desktop/installParts/p" + "." + i + ".dat")));
				//Read rest
				int b;
				while((b = in.read()) != -1) {
					out.write(b);
				}
				out.close();
				try {
					blockList.add(Utils.checksum(new File("C:/Users/Kevin/Desktop/installParts/p" + "." + i + ".dat")));
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(blockList);
		//Make temporary file chunks to checksum them, then delete
		//One time, as once generated blockList, can simply serialize
	}
	
	public void processBlock(String block) {
		for(String str : blockList) {
			if(block.equals(str)) {
				presentBlocks.add(block);
			}
		}
	}
	
	//Used to create a FileSender
	public int getSendBlockNumber() {
		for(int i=0; i < blockList.size(); i++) {
			if(!presentBlocks.contains(blockList.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	public void unify() throws IOException {
		int numberParts = blockList.size();
		String correctedName = file;
		// now, assume that the files are correctly numbered in order (that some joker didn't delete any part)
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(correctedName));
		File[] blocks = new File(getDir()).listFiles();
		for(int part = 0; part < numberParts; part++) {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(blocks[part]));
			int pointer;
			while((pointer = in.read()) != -1) {
				out.write(pointer);
			}
			in.close();
		}
		out.close();
	}
	
	public String getDir() {
		return Utils.defineAppDataDir() + "/" + file;
	}
	
	public boolean relevant(String str) {
		if(file.toLowerCase().contains(str.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		return Utils.base64(file) + "/" + gson.toJson(blockList);
	}
}
