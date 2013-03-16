package backend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FreeSpaceMgnt {
	private static byte[] freeBlockBitmap = new byte[Disk.noOfBlocks];
	private static byte dirtyBuffer = 0;
	
	public static Block getBlock() {
		int freeBlockAddress = 0;
		for(int i = 0; i < Disk.noOfBlocks; i++)
			if(freeBlockBitmap[i] > 0) {
				freeBlockAddress = i;
				freeBlockBitmap[i] = 0;
				dirtyBuffer = 1;
			}
		Block retBlock = null;
		try {
			retBlock = new Block(Disk.transDisk + "/" +String.format("%05d", freeBlockAddress), "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		return retBlock;
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
	
	public static boolean isBufferDirty() {
		if(dirtyBuffer == 0)
			return false;
		else
			return true;
	}
}
