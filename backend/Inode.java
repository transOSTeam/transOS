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
	//17 bytes for \n. So total inodeSize = 119
	public static final int inodeSize = 119;
	
	private byte isDirty;			//dirty bits for modified file content
	
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
		blockPointers = new int[] {0,0,0,0,0};		//5th block pointer is second level indirect pointer. Hard code
		isDirty = 1;
	}
	Inode(int inodeNum){							//constructor to bring existing Inode into memory (read)
		int inodeBlockAdd = this.getInodeAddress(inodeNum);
		try {
			Block inodeBlock = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", inodeBlockAdd), "r");
			inodeBlock.seek(inodeNum%4*inodeSize);
			int intBuffer = inodeBlock.read();			//this can cause trouble!!! how many bytes read? maybe need to do -48
			if(intBuffer == inodeNum) {
				this.inodeNumber = inodeNum;
				this.signature = inodeBlock.read();
				this.blockCount = inodeBlock.read();
				this.fileType = inodeBlock.readChar();
				this.grpId = inodeBlock.read();
				this.hardLinkCount = inodeBlock.read();
				this.refCount = inodeBlock.read();
				this.userId = inodeBlock.read();
				this.accessedTime = Timestamp.valueOf(inodeBlock.readLine());
				this.createdTime = Timestamp.valueOf(inodeBlock.readLine());
				this.modifyTime = Timestamp.valueOf(inodeBlock.readLine());
				for(int i = 0; i < 3; i++)
					this.permission[i] = inodeBlock.readByte();
				for(int i = 0; i < 5; i++)
					this.blockPointers[i] = inodeBlock.read();
				isDirty = 0;
			}
			else {
				System.out.println("Wrong inode read, Inode = "+inodeNum);
			}
			inodeBlock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	Inode(int inodeNum, int userId, int grpId, int pU, int pG, int pW, char fileType){		//pU, pG, pW: permission User, Group, World
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
		isDirty = 1;
	}
	
	private int getInodeAddress(int inodeNumber) {
		int address = 0;
		address = Disk.inodeStartBlock + (int)inodeNumber/4;
		return address;
	}
	public static void resetInodeBlock(File f) {
		long fileSize = f.length();
		boolean firstWrite = false;
		
		while(fileSize <= Disk.maxBlockSize - Inode.inodeSize) {
			Inode tempInode = new Inode();
			String content = String.format("%03d", tempInode.inodeNumber)+"\n"+tempInode.signature+"\n"+String.format("%02d", tempInode.getBlockCount())+"\n"+tempInode.fileType+"\n"+String.format("%03d", tempInode.grpId)+"\n"+String.format("%02d", tempInode.hardLinkCount);
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
		if(this.isDirty == 1) {
			Block inodeB = this.getBlock();
			String content;
			try {
				long seekPos = 0;
				String ip = inodeB.readLine();
				while(ip != null && ip.compareTo(String.format("%03d", this.inodeNumber))!=0) {
					inodeB.seek(inodeB.getFilePointer() - 4);
					seekPos += Inode.inodeSize;
					inodeB.seek(seekPos);
					ip = inodeB.readLine();
				}
				inodeB.seek(inodeB.getFilePointer() - 4);
				content = String.format("%03d", this.inodeNumber)+"\n"+this.signature+"\n"+String.format("%02d", this.getBlockCount())+"\n"+this.fileType+"\n"+String.format("%03d", this.grpId)+"\n"+String.format("%02d", this.hardLinkCount);
				content += "\n"+String.format("%02d", this.refCount)+"\n"+String.format("%03d", this.userId)+"\n";
				content += this.accessedTime.toString().substring(0, 19)+"\n"+this.createdTime.toString().substring(0, 19)+"\n"+this.modifyTime.toString().substring(0, 19)+"\n"+this.permission[0]+this.permission[1]+this.permission[2]+"\n"+String.format("%05d", this.blockPointers[0]);
				content += "\n"+String.format("%05d", this.blockPointers[1])+"\n"+String.format("%05d", this.blockPointers[2])+"\n"+String.format("%05d", this.blockPointers[3])+"\n"+String.format("%05d", this.blockPointers[4])+"\n";
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
			this.isDirty = 0;
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
	public void writeContent(String content) {				//this may blow up!
		FreeSpaceMgnt.consumeBlocks(this.blockPointers);
		int blockPointerIndex = 0;
		int noOfBlocksReq = content.length()/Disk.maxBlockSize;
		int start = 0, end = Math.min(content.length(), Disk.maxBlockSize);
		Block tempBlock = null;
		while(noOfBlocksReq > 0 && blockPointerIndex < 4) {
			tempBlock = FreeSpaceMgnt.getBlock();
			try {
				tempBlock.writeBytes(content.substring(start, end));
				start = end;
				end += Disk.maxBlockSize;
				if(end > content.length())
					end = content.length();
				this.blockPointers[blockPointerIndex++] = tempBlock.getBlockNumber();
				this.blockCount++;
				noOfBlocksReq--;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(noOfBlocksReq > 0) {
			tempBlock = FreeSpaceMgnt.getBlock();
			this.blockPointers[4] = tempBlock.getBlockNumber();
			Block indirectPointerBlk = null;
			try {
				indirectPointerBlk = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", this.blockPointers[4]), "rw");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			int seekSize = 6;			// 5 + 1\n
			while(noOfBlocksReq > 0) {
				tempBlock = FreeSpaceMgnt.getBlock();
				try {
					tempBlock.writeBytes(content.substring(start, end));
					start = end;
					end += Disk.maxBlockSize;
					if(end > content.length())
						end = content.length();
					indirectPointerBlk.writeBytes(String.format("%05d", tempBlock.getBlockNumber()) + "\n");
					indirectPointerBlk.seek(indirectPointerBlk.getFilePointer()+seekSize);
					this.blockCount++;
					noOfBlocksReq--;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.isDirty = 1;
		this.writeToDisk();
	}
	public int getBlockCount() {
		return blockCount;
	}
	
	public int[] getBlockPointers() {
		return this.blockPointers;
	}
	public void releaseBlocks() {
		FreeSpaceMgnt.consumeBlocks(this.blockPointers);
	}
}