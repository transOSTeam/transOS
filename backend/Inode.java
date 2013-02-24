package backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class Inode {
	private static int inodeNumberCounter = 0; 
	
	private int signature;					// 1 |0 inactive, 1 Inode, 2 single indirect data block, 3 double indirect and so on...
	private int inodeNumber;				// 3
	private int refCount;					// 2 | I have no idea what's this for
	private int hardLinkCount;				// 2
	private int userId;						// 3
	private int grpId;						// 3
	private Timestamp accessedTime;			// 19
	private Timestamp modifyTime;			// 19
	private Timestamp createdTime;			// 19
	private int blockCount;					// 2
	private int permission[];				// 3
	private char fileType;					// 1
	private int[] blockPointers;			// 5X5 = 25
	//16 bytes for \n. So total inodeSize = 118
	public static final int inodeSize = 118;
	
	Inode(){
		signature = 0;
		inodeNumber = inodeNumberCounter++;
		refCount = 0;
		hardLinkCount = 0;
		userId = 0;
		grpId = 0;
		java.util.Date date= new java.util.Date();
		createdTime = modifyTime = accessedTime = new Timestamp(date.getTime());
		blockCount = 0;
		permission = new int[]{7,7,7};
		fileType = 'r';
		blockPointers = new int[] {0,0,0,0,0};
	}
	Inode(int inodeNum, int userId, int grpId, int pU, int pG, int pW, char fileType){
		permission = new int[3];
		blockPointers = new int[5];
		
		this.userId = userId;
		this.grpId = grpId;
		this.permission[0] = pU;
		this.permission[1] = pG;
		this.permission[2] = pW;
		this.fileType = fileType;
		
		this.signature = 1;
		this.inodeNumber = inodeNum;
		this.refCount = 0;
		this.hardLinkCount = 0;
		java.util.Date date= new java.util.Date();
		this.createdTime = this.modifyTime = this.accessedTime = new Timestamp(date.getTime());
		blockPointers = new int[5];
		blockPointers[0] = FreeSpaceMgnt.getBlock();
	}
	
	public static void resetInodeBlock(File f) {
		long fileSize = f.length();
		boolean firstWrite = false;
		
		while(fileSize <= Disk.maxBlockSize - Inode.inodeSize) {
			Inode tempInode = new Inode();
			String content = String.format("%03d", tempInode.inodeNumber)+"\n"+tempInode.signature+"\n"+String.format("%02d", tempInode.blockCount)+"\n"+tempInode.fileType+"\n"+String.format("%03d", tempInode.grpId)+"\n"+String.format("%02d", tempInode.hardLinkCount);
			content += "\n"+String.format("%02d", tempInode.refCount)+"\n"+String.format("%03d", tempInode.userId)+"\n";
			content += tempInode.accessedTime.toString().substring(0, 19)+"\n"+tempInode.createdTime.toString().substring(0, 19)+"\n"+tempInode.modifyTime.toString().substring(0, 19)+"\n"+tempInode.permission[0]+tempInode.permission[1]+tempInode.permission[2]+"\n"+String.format("%05d", tempInode.blockPointers[0]);
			content += "\n"+String.format("%05d", tempInode.blockPointers[1])+"\n"+String.format("%05d", tempInode.blockPointers[2])+"\n"+String.format("%05d", tempInode.blockPointers[3])+"\n"+String.format("%05d", tempInode.blockPointers[4]);
			fileSize += content.length();
			FileWriter fw;
			try {
				fw = new FileWriter(f.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				if(firstWrite == false) {
					bw.write(content);
					firstWrite = true;
				}
				else
					bw.write("\n"+content);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void writeToDisk() {
		Block inodeB = this.getBlock();
		String content;
		try {
			long seekSize = Inode.inodeSize + 1;
			while(String.format("%03d", this.inodeNumber).compareTo(inodeB.readLine()) != 0) {
				inodeB.seek(seekSize);
				seekSize += seekSize;
			}
			seekSize -= seekSize;
			inodeB.seek(seekSize);
			content = String.format("%03d", this.inodeNumber)+"\n"+this.signature+"\n"+String.format("%02d", this.blockCount)+"\n"+this.fileType+"\n"+String.format("%03d", this.grpId)+"\n"+String.format("%02d", this.hardLinkCount);
			content += "\n"+String.format("%02d", this.refCount)+"\n"+String.format("%03d", this.userId)+"\n";
			content += this.accessedTime.toString().substring(0, 19)+"\n"+this.createdTime.toString().substring(0, 19)+"\n"+this.modifyTime.toString().substring(0, 19)+"\n"+this.permission[0]+this.permission[1]+this.permission[2]+"\n"+String.format("%05d", this.blockPointers[0]);
			content += "\n"+String.format("%05d", this.blockPointers[1])+"\n"+String.format("%05d", this.blockPointers[2])+"\n"+String.format("%05d", this.blockPointers[3])+"\n"+String.format("%05d", this.blockPointers[4]);
			inodeB.writeBytes(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inodeB.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private Block getBlock() {
		Block retBlock = null;
		try {
			retBlock = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", this.inodeNumber/3 + Disk.inodeStartBlock), "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return retBlock;
	}
}