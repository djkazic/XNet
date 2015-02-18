package blocks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import main.Core;
import main.Utils;

import com.google.gson.Gson;

//Represents files as a file and group of Blocks
//Add self to blockDex so can be searched for
//Folder is directory for chunks if downloading
//Otherwise no folder needed, just get tempBlocks

public class BlockedFile {

	private File file;
	private ArrayList<String> blockList;
	private Gson gson = new Gson();
	private BlockedFileDL bfdl;

	/**
	 * Existing file set (path)
	 * @param filePath
	 */
	public BlockedFile(String filePath) {
		Core.blockDex.add(this);
		this.file = new File(filePath);
		blockList = new ArrayList<String> ();
		getTempBlocks();
		bfdl = new BlockedFileDL(this);
	}
	
	/**
	 * Existing file set (direct)
	 * @param file
	 */
	public BlockedFile(File file) {
		Core.blockDex.add(this);
		this.file = file;
		blockList = new ArrayList<String> ();
		getTempBlocks();
		bfdl = new BlockedFileDL(this);
	}
	
	/**
	 * File does not yet exist
	 * Create download directory
	 * Filepath file set
	 * blockList set
	 */
	public BlockedFile(String file, ArrayList<String> blockList) {
		Core.blockDex.add(this);
		this.file = new File(file);
		this.blockList = blockList;
		bfdl = new BlockedFileDL(this);
		Utils.initAppDataDir(file);
	}
	
	/**
	public static void main(String[] args) {
		Core.blockDex = new ArrayList<BlockedFile> ();
		BlockedFile bf = new BlockedFile("C:/Users/Kevin/Documents/XNet/n-600.pdf");
	}
	**/
	
	/**
	 * Splits BlockedFile into temporary blocks to generate blockList
	 */
	private void getTempBlocks() {
		File mFile = file;
		try {
			double fileLen = (double) mFile.length();
			double numberOfBlocks = (fileLen / Core.chunkSize);
			//System.out.println(numberOfBlocks);
			//Process all complete blocks
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(mFile));
			int i;
			for(i = 0; i < numberOfBlocks - 1; i++) {
				File temp = File.createTempFile("temp", "block");
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
				for(int currentByte = 0; currentByte < Core.chunkSize; currentByte++) {
					out.write(in.read());
				}
				out.close();
				try {
					blockList.add(Utils.checksum(temp));
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
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
				try {
					blockList.add(Utils.checksum(temp));
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				temp.delete();
			} else {
				Utils.print(this, "Temp block sizes check! " + fileLen + " | " + (Core.chunkSize * i));
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the name of a missing block for this BlockedFile
	 * @return
	 */
	public String getNeededBlock() {
		for(int i=0; i < blockList.size(); i++) {
			String currentBlock = blockList.get(i);
			File test = new File(getBlocksDir() + "/" + currentBlock);
			if(!test.exists() || test.length() == 0) {
				return test.getName();
			}
		}
		return null;
		//Completely done, all blocks accounted for
	}
	
	/**
	 * Run when all pieces are received; unifies all blocks and deposits
	 * in regular directory (chunksDir -> regDir)
	 * Also deletes chunksDir, to prevent duplicates
	 * @throws Exception 
	 */
	@SuppressWarnings("resource")
	public void unify() throws Exception {
		int numberParts = blockList.size();
		String outputPath = Utils.defineDir() + "/" + file.getName();
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath));
		File[] blocks = new File(getBlocksDir()).listFiles();
		if(blocks.length != numberParts) {
			throw new Exception("Number of blocks present != number of parts");
		}
		for(int part = 0; part < numberParts; part++) {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(blocks[part]));
			int pointer;
			while((pointer = in.read()) != -1) {
				out.write(pointer);
			}
			in.close();
		}
		out.close();
		new File(getBlocksDir()).delete();
	}
	
	/**
	 * Return plaintext name
	 * @return
	 */
	public String getName() {
		return file.getName();
	}
	
	/**
	 * Returns base64 directory in AppData where blocks are stored
	 * @return
	 */
	public String getBlocksDir() {
		return Utils.defineAppDataDir() + "/" + Utils.base64(file.getName());
	}
	
	public boolean relevant(String str) {
		if(file.getName().toLowerCase().contains(str.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		return Utils.base64(file.getName()) + "/" + gson.toJson(blockList);
	}
	
	public void download() {
		Thread bfdlThread = (new Thread(bfdl));
		bfdlThread.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public BlockedFileDL getDL() {
		return bfdl;
	}
	
	public ArrayList<String> getBlockList() {
		return blockList;
	}

	public int getBlockNumber(String blockName) {
		int prelim = blockList.indexOf(blockName);
		if(prelim == -1) {
			return -1;
		}
		return prelim;
	}
}
