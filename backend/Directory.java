package backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;


public class Directory {
	private int inodeNum;
	private HashMap<Integer, String> dirContent;  
	
	Directory(int inodeNum){
		this.inodeNum = inodeNum;
		dirContent = new HashMap<Integer, String>();
		Inode dirInode = new Inode(inodeNum);
		int[] blkPointers = dirInode.getBlockPointers();
		for(int i = 0; i < dirInode.getBlockCount(); i++) {
			try {
				Block tempBlk = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", blkPointers[i]), "r");
				for(String tempBuffer = tempBlk.readLine(); tempBuffer != null; ) {
					int tempInode = Integer.parseInt(tempBuffer.substring(0, 3));
					String tempName = tempBuffer.substring(4);
					this.dirContent.put(tempInode, tempName);
				}
				tempBlk.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Inode makeFile(String fileName, String fileContent) {
		int inodeNum = FreeSpaceMgnt.getInode();
		Inode newFileInode = new Inode(inodeNum, 0, 0, 7, 5, 5, 'r');
		newFileInode.writeContent(fileContent);
		newFileInode.writeToDisk();
		this.dirContent.put(inodeNum, fileName);
		Inode thisInode = new Inode(this.inodeNum);
		//thisInode.writeToDisk(); this will write inode to disk..we need to write dir content to disk
		//we'll need dirty bit for every block pointer
		//anurag@19march | 11pm
		return newFileInode;
	}
	
	public void deleteFile(int victimInodeNo) {
		Inode victimInode = new Inode(victimInodeNo);
		FreeSpaceMgnt.consumeBlock(victimInode.getBlockPointers());
		FreeSpaceMgnt.consumeInode(victimInodeNo);
		this.dirContent.remove(victimInode);
		//write the changed data to disk
	}
	
	public Inode makeDir(String dirName) {
		int inodeNum = FreeSpaceMgnt.getInode();
		Inode newDirInode = new Inode(inodeNum, 0, 0, 7, 5, 5, 'd');
		newDirInode.writeToDisk();
		this.dirContent.put(inodeNum, dirName);
		Inode thisInode = new Inode(this.inodeNum);
		//thisInode.writeToDisk(); this will write inode to disk..we need to write dir content to disk
		return newDirInode;
	}
	
}
