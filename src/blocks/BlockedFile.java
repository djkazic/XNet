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
	private ArrayList<String> presentBlocks = new ArrayList<String> ();
	private Gson gson = new Gson();
	private BlockedFileDL bfdl;

	/**
	 * Existing file set (path)
	 * @param filePath
	 */
	public BlockedFile(String filePath) {
		this.file = new File(filePath);
		blockList = new ArrayList<String> ();
		getTempBlocks();
		Core.blockDex.add(this);
	}
	
	/**
	 * Existing file set (direct)
	 * @param file
	 */
	public BlockedFile(File file) {
		this.file = file;
		blockList = new ArrayList<String> ();
		getTempBlocks();
		Core.blockDex.add(this);
	}
	
	/**
	 * File does not yet exist
	 * Create download directory
	 * Filepath file set
	 * blockList set
	 */
	public BlockedFile(String file, ArrayList<String> blockList) {
		this.file = new File(file);
		this.blockList = blockList;
		Utils.initAppDataDir(file);
		Core.blockDex.add(this);
	}
	
	/**
	 * Tester main
	public static void main(String[] args) {
		Core.blockDex = new ArrayList<BlockedFile> ();
		BlockedFile bf = new BlockedFile("C:/Users/caik/Documents/XNet/Minecraft.exe");
		System.out.println(bf);
		System.out.println(bf.getNeededBlock());
		//Simulate completely done
		ArrayList<String> allBlocks = bf.getBlockList();
		for(String bl : allBlocks) {
			bf.logBlock(bl);
		}
		System.out.println(bf.getNeededBlock());
		//System.out.println(bf.getChunksDir());
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
				for (int currentByte = 0; currentByte < Core.chunkSize; currentByte++) {
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
	
	public void logBlock(String block) {
		for(String str : blockList) {
			if(block.equals(str)) {
				presentBlocks.add(block);
			}
		}
	}
	
	/**
	 * Returns the name of a missing block for this BlockedFile
	 * @return
	 */
	public String getNeededBlock() {
		for(int i=0; i < blockList.size(); i++) {
			if(!presentBlocks.contains(blockList.get(i))) {
				return blockList.get(i);
			}
		}
		return null;
		//Completely done, all blocks accounted for
	}
	
	/**
	 * Run when all pieces are received; unifies all blocks and deposits
	 * in regular directory (chunksDir -> regDir)
	 * Also deletes chunksDir, as it is no longer needed
	 * @throws IOException
	 */
	public void unify() throws IOException {
		int numberParts = blockList.size();
		String outputPath = Utils.defineDir() + "/" + file.getName();
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath));
		File[] blocks = new File(getBlocksDir()).listFiles();
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
		Utils.print(this, "Created BlockedFileDL thread in preparation of blocks download");
		bfdl = new BlockedFileDL(this);
		(new Thread(bfdl)).start();
	}
	
	public BlockedFileDL getDL() {
		return bfdl;
	}
	
	public ArrayList<String> getBlockList() {
		return blockList;
	}
}