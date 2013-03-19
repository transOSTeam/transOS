package backend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FreeSpaceMgnt {
	private static byte[] freeBlockBitmap = new byte[Disk.noOfBlocks];
	private static byte[] freeInodeBitmap = new byte[Disk.inodeEndBlock - Disk.inodeStartBlock + 1];
	private static byte[] dirtyBuffer = {0,0};					//0: inode buffer; 1: freeBlock buffer;
	
	public static Block getBlock() {
		int freeBlockAddress = 0;
		for(int i = 0; i < Disk.noOfBlocks; i++)
			if(freeBlockBitmap[i] > 0) {
				freeBlockAddress = i;
				freeBlockBitmap[i] = 0;
				dirtyBuffer[1] = 1;
			}
		Block retBlock = null;
		try {
			retBlock = new Block(Disk.transDisk + "/" +String.format("%05d", freeBlockAddress), "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		return retBlock;
	}
	public static boolean consumeBlock(Block victim) {
		boolean retCode = false;
		freeBlockBitmap[victim.getBlockNumber()] = 0;
		dirtyBuffer[1] = 1;
		return retCode;
	}
	public static boolean consumeBlock(Block[] victims) {
		boolean retCode = false;
		for(Block tempBlock: victims) {
			freeBlockBitmap[tempBlock.getBlockNumber()] = 0;
		}
		dirtyBuffer[1] = 1;
		return retCode;
	}
	public static void initBitmap(int[] freeBlockBitmapNo) {
		try {
			for(int j = 0; j < 4; j++) {
				BufferedReader br = new BufferedReader(new FileReader(Disk.transDisk + "/" +String.format("%05d", freeBlockBitmapNo[j])));
				int ip;
				for(int i = 0; (ip = br.read())>0; i++)
					FreeSpaceMgnt.freeBlockBitmap[j*Disk.maxBlockSize + i] = (byte) (ip - 48);
				br.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void initInodeBitmap(int inodeBitmapBlockNo) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(Disk.transDisk + "/" + String.format("%05d", inodeBitmapBlockNo)));
			int ip;
			for(int i = 0; (ip = br.read()) >0; i++) 
				FreeSpaceMgnt.freeInodeBitmap[i] = (byte) (ip - 48);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void shutDown() {
		Block superBlock = null;
		try {
			superBlock = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", Disk.superBlockAddress),"r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if(dirtyBuffer[0] == 1) {				//write dirty inode buffer to disk
			int noOfBlocksBitmap = 4; 					// hard code: 2000sized bitmap will require 4 blocks of 500
			try {
				superBlock.readLine();
				int[] freeBlockBitmapNo = new int[noOfBlocksBitmap];
				String tempStr = superBlock.readLine();
				for(int i = 0; i < noOfBlocksBitmap; i++)
					freeBlockBitmapNo[i] = Integer.parseInt(tempStr.substring(i, i+1));
				
				for(int i = 0, off = 0; i < 4; i++, off += 500) {
					Block bitmapBlock = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", freeBlockBitmapNo[i]),"rw");
					bitmapBlock.write(freeBlockBitmap, off, 500);			//..test this!!!
					bitmapBlock.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		if(dirtyBuffer[1] == 1) {				//write dirty block buffer to disk
			try {
				int freeInodeBitmapNo = superBlock.read();
				Block freeInodeBitmapBlk = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", freeInodeBitmapNo),"rw");
				freeInodeBitmapBlk.write(freeInodeBitmap);
				freeInodeBitmapBlk.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
