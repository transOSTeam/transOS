package backend.disk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;


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
	public Inode(int inodeNum){							//constructor to bring existing Inode into memory (read)
		int inodeBlockAdd = this.getInodeAddress(inodeNum);
		try {
			Block inodeBlock = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", inodeBlockAdd) + ".txt", "r");
			inodeBlock.seek(inodeNum%4*inodeSize);
			int intBuffer = Integer.parseInt(inodeBlock.readLine());
			if(intBuffer == inodeNum) {
				this.inodeNumber = inodeNum;
				this.signature = Integer.parseInt(inodeBlock.readLine());
				this.blockCount = Integer.parseInt(inodeBlock.readLine());
				this.fileType = inodeBlock.readLine().charAt(0);
				this.grpId = Integer.parseInt(inodeBlock.readLine());
				this.hardLinkCount = Integer.parseInt(inodeBlock.readLine());
				this.refCount = Integer.parseInt(inodeBlock.readLine());
				this.userId = Integer.parseInt(inodeBlock.readLine());
				this.accessedTime = Timestamp.valueOf(inodeBlock.readLine());
				this.createdTime = Timestamp.valueOf(inodeBlock.readLine());
				this.modifyTime = Timestamp.valueOf(inodeBlock.readLine());
				String permissions = inodeBlock.readLine();
				this.permission = new int[3];
				for(int i = 0; i < 3; i++)
					this.permission[i] = Integer.parseInt(permissions.substring(i,i+1));
				this.blockPointers = new int[5];
				for(int i = 0; i < 5; i++)
					this.blockPointers[i] = Integer.parseInt(inodeBlock.readLine());
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
		this.hardLinkCount = 1;
		java.util.Date date= new java.util.Date();
		this.createdTime = this.modifyTime = this.accessedTime = new Timestamp(date.getTime());
		blockPointers = new int[5];
		isDirty = 1;
	}
	
	public Inode duplicate() {
		int newInodeNo = FreeSpaceMgnt.getInode();
		Inode newInode = new Inode(newInodeNo, this.userId,this.grpId, this.permission[0], this.permission[1], this.permission[2], this.fileType);
		return newInode;
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
			java.util.Date date= new java.util.Date();
			this.modifyTime = new Timestamp(date.getTime());
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
			retBlock = new Block(this.inodeNumber/4 + Disk.inodeStartBlock, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return retBlock;
	}
	public void writeContent(String content) {				//this may blow up! | ugly hack @25march
		FreeSpaceMgnt.consumeBlocks(this.blockPointers);
		this.blockCount = 0;
		int blockPointerIndex = 0;
		float blockSize = Disk.maxBlockSize;
		int noOfBlocksReq = (int) Math.ceil(content.length()/blockSize);
		int start = 0, end = Math.min(content.length(), Disk.maxBlockSize);
		Block tempBlock = null;
		while(noOfBlocksReq > 0 && blockPointerIndex < 4) {
			int tempBlockNo = FreeSpaceMgnt.getBlockNo();		//get block no..delete file..create file..create block then write
			File tempBlockFile = new File(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", tempBlockNo) + ".txt");
			if(tempBlockFile.exists())
				tempBlockFile.delete();
			try {
				tempBlockFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				tempBlock = new Block(tempBlockNo, "rw");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
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
			int tempBlockNo = FreeSpaceMgnt.getBlockNo();
			File tempBlockFile = new File(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", tempBlockNo) + ".txt");
			if(tempBlockFile.exists())
				tempBlockFile.delete();
			try {
				tempBlockFile.createNewFile();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			try {
				tempBlock = new Block(tempBlockNo, "rw");
			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
			}
			this.blockPointers[4] = tempBlock.getBlockNumber();
			Block indirectPointerBlk = null;
			try {
				indirectPointerBlk = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", this.blockPointers[4]) + ".txt", "rw");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			while(noOfBlocksReq > 0) {
				tempBlockNo = FreeSpaceMgnt.getBlockNo();
				File tempBlockFile1 = new File(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", tempBlockNo) + ".txt");
				if(tempBlockFile1.exists())
					tempBlockFile1.delete();
				try {
					tempBlockFile1.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					tempBlock = new Block(tempBlockNo, "rw");
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				try {
					tempBlock.writeBytes(content.substring(start, end));
					start = end;
					end += Disk.maxBlockSize;
					if(end > content.length())
						end = content.length();
					indirectPointerBlk.writeBytes(String.format("%05d", tempBlock.getBlockNumber()) + "\n");
					this.blockCount++;
					noOfBlocksReq--;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				indirectPointerBlk.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.isDirty = 1;
		this.writeToDisk();
	}
	public int getBlockCount() {
		return blockCount;
	}
	public String getFileContent() {
		String content = "";
		for(int i = 0; i < 4; i++) {
			try {
				if(this.blockPointers[i] != 0) {
					Block tempBlock = new Block(this.blockPointers[i], "r");
					content += tempBlock.getContent();
					tempBlock.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		if(this.blockPointers[4] != 0) {
			try {
				Block indirectPtBlock = new Block(this.blockPointers[4],"r");
				String blkBuffer;
				while((blkBuffer = indirectPtBlock.readLine()) != null) {
					Block tempBlock = new Block(Integer.parseInt(blkBuffer), "r");
					content += tempBlock.getContent();
					tempBlock.close();
				}
				indirectPtBlock.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		return content;
	}
	public int[] getBlockPointers() {
		return this.blockPointers;
	}
	public void releaseBlocks() {
		this.blockCount = 0;
		FreeSpaceMgnt.consumeBlocks(this.blockPointers);
	}
	public char getFileType() {
		return this.fileType;
	}
	public int getInodeNum(){
		return this.inodeNumber;
	}
	public Timestamp getAccessedTime(){
		return this.accessedTime;
	}
	public Timestamp getModifiedTime(){
		return this.modifyTime;
	}
	public Timestamp getCreatedTime(){
		return this.createdTime;
	}
	public int[] getPermissions(){
		return this.permission;
	}
	public int getUserId() {
		return this.userId;
	}
	public int getGrpId() {
		return this.grpId;
	}
	public void setPermissions(int[] perm) {
		this.permission = perm;
		this.isDirty = 1;
	}
	public void setUserId(int newUserId) {
		this.userId = newUserId;
		this.isDirty = 1;
	}
	public ArrayList<Integer> getIndirectBlocks() {
		ArrayList<Integer> pointers = null;
		if(this.blockPointers[4] != 0) {
			pointers = new ArrayList<Integer>();
			try {
				Block indirectPointerBlk = new Block(this.blockPointers[4], "r");
				String buffer;
				while((buffer = indirectPointerBlk.readLine()) != null) {
					pointers.add(Integer.parseInt(buffer));
				}
				indirectPointerBlk.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		return pointers;
	}
	public void hardLinkpp() {
		this.hardLinkCount++;
		this.isDirty = 1;
	}
	public void hardLinkmm() {
		this.hardLinkCount--;
		this.isDirty = 1;
	}
	public int getHardLinkCount() {
		return this.hardLinkCount;
	}
}