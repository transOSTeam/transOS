package backend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FreeSpaceMgnt {
	private static byte[] freeBlockBitmap = new byte[Disk.noOfBlocks];
	private static byte dirtyBuffer = 0;
	
	public static int getBlock() {
		int freeBlockAddress = 0;
		for(int i = 0; i < Disk.noOfBlocks; i++)
			if(freeBlockBitmap[i] > 0) {
				freeBlockAddress = i;
				freeBlockBitmap[i] = 0;
				dirtyBuffer = 1;
			}
				
		return freeBlockAddress;
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
		/*for(int i = 0; i < Disk.inodeEndBlock; i++)
			System.out.println(FreeSpaceMgnt.freeBlockBitmap[i]);*/
	}
	
	public static boolean isBufferDirty() {
		if(dirtyBuffer == 0)
			return false;
		else
			return true;
	}
}
