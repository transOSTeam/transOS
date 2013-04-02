package backend.disk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;


public class FreeSpaceMgnt {
	private static byte[] freeBlockBitmap = new byte[Disk.noOfBlocks];
	private static byte[] freeInodeBitmap = new byte[(Disk.inodeEndBlock - Disk.inodeStartBlock + 1)*(Disk.maxBlockSize/Inode.inodeSize)];
	private static byte[] dirtyBuffer = {0,0};					//0: inode buffer; 1: freeBlock buffer;
	
	public static int getBlockNo() {
		int freeBlockAddress = 0;
		for(int i = 0; i < Disk.noOfBlocks; i++)
			if(freeBlockBitmap[i] == 0) {
				freeBlockAddress = i;
				freeBlockBitmap[i] = 1;
				dirtyBuffer[1] = 1;
				break;
			}
		return freeBlockAddress;
	}
	public static int getInode() {
		int inodeNum = 0;
		for(int i = 0; i < (Disk.inodeEndBlock - Disk.inodeStartBlock + 1)*4; i++) {
			if(freeInodeBitmap[i] == 0) {
				inodeNum = i;
				freeInodeBitmap[i] = 1;
				dirtyBuffer[0] = 1;
				break;
			}
		}
		return inodeNum;
	}
	public static void consumeBlock(int victim) {
		freeBlockBitmap[victim] = 0;
		dirtyBuffer[1] = 1;
	}
	public static void consumeBlocks(int[] victims) {			//we consider 5th entry (index 4) is a indirect block pointer
		for(int i = 0; i < 4; i++) {
			if(victims[i] != 0) {
				freeBlockBitmap[victims[i]] = 0;
			}
		}
		if(victims[4] != 0) {		//indirect block pointer
			try {
				Block indirectPointerBlk = new Block(victims[4], "rw");
				String ip;
				while(( ip = indirectPointerBlk.readLine()) != null ) {
					int tempBlkNo = Integer.parseInt(ip);
					FreeSpaceMgnt.consumeBlock(tempBlkNo);
				}
				indirectPointerBlk.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			FreeSpaceMgnt.consumeBlock(victims[4]);
		}
		
		dirtyBuffer[1] = 1;
	}
	public static void init() {
		Block superBlock = null;
		try {
			superBlock = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", Disk.superBlockAddress),"r");
			superBlock.readLine();
			int[] freeBlockBitmapNo = new int[4];
			String bitmap = superBlock.readLine();
			for(int i = 0; i < 4; i++) {
				freeBlockBitmapNo[i] = Integer.parseInt(bitmap.substring(i, i+1));
			}
			FreeSpaceMgnt.initBitmap(freeBlockBitmapNo);
			int inodeBitmapBlockNo = Integer.parseInt(superBlock.readLine());
			FreeSpaceMgnt.initInodeBitmap(inodeBitmapBlockNo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void initBitmap(int[] freeBlockBitmapNo) {
		try {
			for(int j = 0; j < 4; j++) {
				Block bitMapBlock = new Block(freeBlockBitmapNo[j],"r");
				byte ip;
				for(int i = 0; i < bitMapBlock.length(); i++) {
					ip = bitMapBlock.readByte();
					FreeSpaceMgnt.freeBlockBitmap[j*Disk.maxBlockSize + i] = (byte) (ip - 48);
				}
				bitMapBlock.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void initInodeBitmap(int inodeBitmapBlockNo) {
		try {
			Block inodeBitmapBlk = new Block(inodeBitmapBlockNo, "r");
			byte ip;
			for(int i = 0; i < inodeBitmapBlk.length(); i++) {
				ip = inodeBitmapBlk.readByte();
				FreeSpaceMgnt.freeInodeBitmap[i] = (byte) (ip - 48);
			}
			inodeBitmapBlk.close();
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
		
		if(dirtyBuffer[1] == 1) {				//write dirty block buffer to disk
			int noOfBlocksBitmap = 4; 					// hard code: 2000sized bitmap will require 4 blocks of 500
			try {
				superBlock.readLine();
				int[] freeBlockBitmapNo = new int[noOfBlocksBitmap];
				String tempStr = superBlock.readLine();
				for(int i = 0; i < noOfBlocksBitmap; i++)
					freeBlockBitmapNo[i] = Integer.parseInt(tempStr.substring(i, i+1));
				
				byte[] bitmapPieces;
				for(int i = 0, off = 0; i < 4; i++, off += 500) {
					Block bitmapBlock = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", freeBlockBitmapNo[i]),"rw");
					bitmapPieces = FreeSpaceMgnt.prepBuffer(Arrays.copyOfRange(freeBlockBitmap, off, off + 500));
					bitmapBlock.write(bitmapPieces);			//..test this!!!
					bitmapBlock.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(dirtyBuffer[0] == 1) {				//write dirty inode buffer to disk
			try {
				superBlock.seek(0);
				superBlock.readLine();
				superBlock.readLine();
				int freeInodeBitmapNo = Integer.parseInt(superBlock.readLine());
				Block freeInodeBitmapBlk = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", freeInodeBitmapNo),"rw");
				freeInodeBitmapBlk.write(prepBuffer(freeInodeBitmap));
				freeInodeBitmapBlk.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private static byte[] prepBuffer(byte[] input) {
		byte [] output = new byte[input.length];
		for(int i = 0; i < input.length; i++) {
			output[i] = (byte) (input[i] + 48);
		}
		return output;
	}
	public static void consumeInode(int victimInodeNo) {
		FreeSpaceMgnt.freeInodeBitmap[victimInodeNo] = 0;
		dirtyBuffer[0] = 1;
	}
}
