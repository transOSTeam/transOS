package backend.disk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import backend.User;



public class Disk {
	
	public static final File homeDir = new File(System.getProperty("user.home"));	
	public static final File transDisk = new File(homeDir.toString() + "/TransDisk");
	public static final File tmpFolder = new File(homeDir.toString() + "/TransOSTemp");
	
	public static final int maxBlockSize = 500;											// in B
	public static final int diskSize = 1;												// in MB
	public static final int noOfBlocks = Disk.diskSize*1000*1000/Disk.maxBlockSize;
	
	public static final int inodeStartBlock = 10;
	public static final int inodeEndBlock = 100;
	
	public static final int partitionTableAddress = 0;
	public static final int superBlockAddress = 2;
	
	public static int tempFileCounter = 0;
	
	public static void createDisk(){
		createRoot();
		createBlocks();
		makePartitionTable();
		initializeInodes();
		initializeFreeSpaceMgnt();
		FreeSpaceMgnt.init();
		User.initUserMgnt();
		createRootDir();
		User.createNewUser("root", "root123", "none");
	}
	
	private static void initializeFreeSpaceMgnt() {
		try {
			//initialize free block bitmap
			int noOfBlocksBitmap = 4; 					// hard code: 2000sized bitmap will require 4 blocks of 500
			Block superBlock = new Block(homeDir.toString() + "/TransDisk/" + String.format("%05d", Disk.superBlockAddress),"r");
			superBlock.readLine();
			int[] freeBlockBitmapNo = new int[noOfBlocksBitmap];
			String tempStr = superBlock.readLine();
			for(int i = 0; i < noOfBlocksBitmap; i++)
				freeBlockBitmapNo[i] = Integer.parseInt(tempStr.substring(i, i+1));
			
			byte[][] freeSpaceBitmapContent = new byte[4][];
			for(int i = 0; i < 4; i++)
				freeSpaceBitmapContent[i] = new byte[Disk.maxBlockSize];
			
			int i = 0;
			for(int k = 0; k < 4; k++) {
				for(int j = i % Disk.maxBlockSize; j < Disk.maxBlockSize; j++, i++) {
					if(i <= Disk.inodeEndBlock)				//all blocks up to last Inode block are considered to be system used and not available for user data
						freeSpaceBitmapContent[k][j] = 49;	//in ASCII 49 is 1
					else
						freeSpaceBitmapContent[k][j] = 48;
				}
			}
			
			for(i = 0; i < 4; i++) {
				Block bitmapBlock = new Block(homeDir.toString() + "/TransDisk/" + String.format("%05d", freeBlockBitmapNo[i]),"rw");
				bitmapBlock.write(freeSpaceBitmapContent[i]);
				bitmapBlock.close();
			}
			
			//initialize free inodes bitmap
			System.out.println(superBlock.getFilePointer());
			int freeInodeBitmapNo = superBlock.read() - 48;
			System.out.println(superBlock.getFilePointer());
			byte freeInodeContent[] = new byte[(Disk.inodeEndBlock- Disk.inodeStartBlock + 1)*4];
			freeInodeContent[0] = freeInodeContent[1] = freeInodeContent[2] = 49;					//blocking Inodes
			for(i = 3; i < (Disk.inodeEndBlock - Disk.inodeStartBlock + 1)*4; i++) {
				freeInodeContent[i] = 48;
			}
			Block freeInodeBitmapBlk = new Block(homeDir.toString() + "/TransDisk/" + String.format("%05d", freeInodeBitmapNo), "rw");
			freeInodeBitmapBlk.write(freeInodeContent);
			freeInodeBitmapBlk.close();
			superBlock.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createRootDir() {
		Inode rootDirInode = new Inode(2,1,1,6,4,4,'d');
		String content = "d "+String.format("%03d", 2)+"\t.\nd "+String.format("%03d", 2)+"\t..\n";
		rootDirInode.writeContent(content);
		rootDirInode.writeToDisk();
	}

	private static void initializeInodes() {
		for(int i = inodeStartBlock; i <= inodeEndBlock; i++){
			File f = new File(homeDir.toString() + "/TransDisk/" + String.format("%05d", i));
			if(!f.exists()){
				System.out.println("Fatal Error...block "+i+" not present");
			}
			else{
				Inode.resetInodeBlock(f);
			}
		}
	}

	private static void createRoot(){
		if(!transDisk.exists()){
			if(transDisk.mkdir()){
				System.out.println("root created");
			}
			else{
				System.out.println("root creation failed");
			}
		}
		else{
			System.out.println(transDisk.length());
			System.out.println("root dir present");
		}
	}
	
	private static void createBlocks(){
		int noOfBlocks = Disk.noOfBlocks;
		for(int i = 0; i < noOfBlocks; i++){
			File f = new File(homeDir.toString() + "/TransDisk/" + String.format("%05d", i));
			if(!f.exists()){
				try {
					f.createNewFile();	
					System.out.println("\t"+i+ " created");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				System.out.println("block present");
			}
		}
	}
	private static void makePartitionTable(){
		try {
			String content;// = "//Partition Table\n//Status\tStart\tEnd";			//4th entry omitted
			
			File partitionTable = new File(Disk.transDisk + "/" +String.format("%05d", Disk.partitionTableAddress));
 
			if (!partitionTable.exists()) {
				System.out.println("Partition Table Not found");
			}
			
			//_________Hard coding partition___________________
			content = "11111\t" + String.format("%05d", 1) + "\t" + String.format("%05d", 10);
			
			FileWriter fw = new FileWriter(partitionTable.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			final String fsBitmapAddress = "5678\n9";			//thats 5,6,7 and 8 and 9 has free Inode bitmap
			Block superBlock = new Block(Disk.transDisk + "/" +String.format("%05d", Disk.superBlockAddress),"rw");
			String content = Disk.noOfBlocks + "\n" + fsBitmapAddress;
			superBlock.write(content.getBytes());
			superBlock.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void bootUp() {
		//create temp space
		if(!tmpFolder.exists())
			tmpFolder.mkdir();
		tmpFolder.deleteOnExit();
		FreeSpaceMgnt.init();		//part of boot-up
	}
	
	public static void shutDown() {
		FreeSpaceMgnt.shutDown();
		if(tmpFolder.exists())
			tmpFolder.delete();
	}
}
