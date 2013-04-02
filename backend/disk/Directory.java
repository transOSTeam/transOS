package backend.disk;


import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;



//import backend.Directory.DirEntry;


public class Directory {
	private int inodeNum;
	//private int parentInodeNum;
	private HashMap<Integer, DirEntry> dirContent;  
	
	public Directory(int inodeNum){
		this.inodeNum = inodeNum;
		dirContent = new HashMap<Integer, DirEntry>();
		Inode dirInode = new Inode(inodeNum);
		int[] blkPointers = dirInode.getBlockPointers();
		for(int i = 0; i < dirInode.getBlockCount(); i++) {
			try {
				Block tempBlk = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", blkPointers[i]), "r");
				for(String tempBuffer = tempBlk.readLine(); tempBuffer != null;tempBuffer = tempBlk.readLine()) {
					char tempType = tempBuffer.charAt(0);						//string is going to like this: "r 123\tfolder1\n"
					int tempInode = Integer.parseInt(tempBuffer.substring(2, 5));
					String tempName = tempBuffer.substring(6);
					/*if(tempName.compareTo("..") == 0)
						this.parentInodeNum = tempInode;*/
					DirEntry tempDirEntry = new DirEntry(tempName, tempType);
					this.dirContent.put(tempInode, tempDirEntry);
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
		DirEntry tempDirEntry = new DirEntry(fileName, 'r');
		this.dirContent.put(inodeNum, tempDirEntry);
		this.writeToDisk();
		return newFileInode;
	}
	
	public void deleteFile(int victimInodeNo) {
		Inode victimInode = new Inode(victimInodeNo);
		if(victimInode.getFileType() == 'd') {
			Directory victimDir = new Directory(victimInodeNo);
			Iterator<Entry<Integer, DirEntry>> dirEntriesNavi = victimDir.dirContent.entrySet().iterator();
			while(dirEntriesNavi.hasNext()) {
				Map.Entry<Integer, DirEntry> pairs = (Map.Entry<Integer, DirEntry>)dirEntriesNavi.next();
				if(pairs.getValue().getName().compareTo(".") != 0 && pairs.getValue().getName().compareTo("..") != 0 )
					victimDir.deleteFile(pairs.getKey());
			}
		}
		victimInode.releaseBlocks();
		FreeSpaceMgnt.consumeInode(victimInodeNo);
		this.dirContent.remove(victimInodeNo);
		this.writeToDisk();
	}
	
	public Inode makeDir(String dirName) {
		int inodeNum = FreeSpaceMgnt.getInode();
		Inode newDirInode = new Inode(inodeNum, 0, 0, 7, 5, 5, 'd');
		String content = "d "+String.format("%03d", newDirInode.getInodeNum())+"\t.\nd "+String.format("%03d", this.inodeNum)+"\t..\n";
		newDirInode.writeContent(content);
		newDirInode.writeToDisk();
		DirEntry tempDirEntry = new DirEntry(dirName, 'd');
		this.dirContent.put(inodeNum, tempDirEntry);
		this.writeToDisk();
		return newDirInode;
	}
	
	private void writeToDisk() {
		String content = new String();
		Iterator<Entry<Integer, DirEntry>> dirEntriesNavi = this.dirContent.entrySet().iterator();
		while(dirEntriesNavi.hasNext()) {
			Map.Entry<Integer, DirEntry> pairs = (Map.Entry<Integer, DirEntry>)dirEntriesNavi.next();
			DirEntry tempVal = (DirEntry) pairs.getValue();
			Integer tempKey = (Integer) pairs.getKey();
			content += tempVal.getType() + " " + String.format("%03d", tempKey.intValue()) + "\t" + tempVal.getName() + "\n";		
		}
		Inode thisInode = new Inode(this.inodeNum);
		thisInode.writeContent(content);
		thisInode.writeToDisk();
	}
	
	public HashMap<Integer, DirEntry> getDirContent(){
		return this.dirContent;
	}
	
	public int searchDir(String name) {				//linear search
		int inodeNum = 0;
		Iterator<Entry<Integer, DirEntry>> dirEntriesNavi = this.dirContent.entrySet().iterator();
		while(dirEntriesNavi.hasNext()) {
			Map.Entry<Integer, DirEntry> pairs = (Map.Entry<Integer, DirEntry>)dirEntriesNavi.next();
			if(pairs.getValue().getName() == name) {
				inodeNum = pairs.getKey();
				break;
			}
		}
		return inodeNum;
	}
	
	public void renameFile(int targetInodeNum, String newFileName) {
		this.dirContent.remove(targetInodeNum);
		this.dirContent.put(targetInodeNum, new DirEntry(newFileName, 'd'));
		this.writeToDisk();
	}
	
	public void copy(int srcFileInode, int sourceDirInode) {
		Inode srcInode = new Inode(srcFileInode);
		Inode targetInode = srcInode.duplicate();
		if(srcInode.getFileType() == 'd') {
			Directory victimDir = new Directory(srcFileInode);
			String newDirContent = "d "+String.format("%03d", targetInode.getInodeNum())+"\t.\nd "+String.format("%03d", this.inodeNum)+"\t..\n";
			targetInode.writeContent(newDirContent);
			targetInode.writeToDisk();
			Directory targetDir = new Directory(targetInode.getInodeNum());
			Iterator<Entry<Integer, DirEntry>> dirEntriesNavi = victimDir.dirContent.entrySet().iterator();
			while(dirEntriesNavi.hasNext()) {
				Map.Entry<Integer, DirEntry> pairs = (Map.Entry<Integer, DirEntry>)dirEntriesNavi.next();
				if(pairs.getValue().getName().compareTo(".") != 0 && pairs.getValue().getName().compareTo("..") != 0 )
					targetDir.copy(pairs.getKey(), srcFileInode);		
			}
		}
		else if(srcInode.getFileType() == 'r') {
			String content = srcInode.getFileContent();
			targetInode.writeContent(content);
			targetInode.writeToDisk();
		}
		Directory srcDir = new Directory(sourceDirInode);
		DirEntry srcEntry = srcDir.dirContent.get(srcFileInode);
		DirEntry tempDirEntry = new DirEntry(srcEntry.getName(), srcEntry.getType());
		this.dirContent.put(targetInode.getInodeNum(), tempDirEntry);
		this.writeToDisk();
	}
	public void move(int srcFileInode, int srcDirInode) {	//move and delete original entry
		this.copy(srcFileInode, srcDirInode);
		Directory victimParentDir = new Directory(srcDirInode);
		victimParentDir.deleteFile(srcFileInode);
	}
	
	public void editFile(int fileInodeNum) {
		Inode fileInode = new Inode(fileInodeNum);
		if(fileInode.getFileType() == 'r') {
			File tempFile = null;
			String content = fileInode.getFileContent();
			try {
				tempFile = new File(Disk.tmpFolder.toString() + "/" + fileInodeNum + ".txt");
				tempFile.deleteOnExit();
				RandomAccessFile tempRF = new RandomAccessFile(tempFile, "rw");
				tempRF.writeBytes(content);
				Desktop.getDesktop().open(tempFile);
				tempRF.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}