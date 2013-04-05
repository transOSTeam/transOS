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

import backend.TransSystem;
import backend.User;



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
		if(this.isWritable(new Inode(this.inodeNum))) {
			int inodeNum = FreeSpaceMgnt.getInode();
			Inode newFileInode = new Inode(inodeNum, TransSystem.getUser().getUserId(), TransSystem.getUser().getGrpId(), 6, 4, 4, 'r');
			newFileInode.writeContent(fileContent);
			newFileInode.writeToDisk();
			DirEntry tempDirEntry = new DirEntry(fileName, 'r');
			this.dirContent.put(inodeNum, tempDirEntry);
			this.writeToDisk();
			return newFileInode;
		}
		else {
			//permission denied
			return null;
		}
	}
	
	public void deleteFile(int victimInodeNo) {
		Inode victimInode = new Inode(victimInodeNo);
		Inode victimsFolder = new Inode(this.inodeNum);
		if(this.isWritable(victimInode) && this.isWritable(victimsFolder)) {
			System.out.println("in del");
			if(victimInode.getFileType() == 'd') {
				Directory victimDir = new Directory(victimInodeNo);
				int[] tempInode = new int[victimDir.dirContent.size() - 2];
				Iterator<Entry<Integer, DirEntry>> dirEntriesNavi = victimDir.dirContent.entrySet().iterator();
				int i = 0;
				while(dirEntriesNavi.hasNext()) {
					Map.Entry<Integer, DirEntry> pairs = (Map.Entry<Integer, DirEntry>)dirEntriesNavi.next();
					if(pairs.getValue().getName().compareTo(".") != 0 && pairs.getValue().getName().compareTo("..") != 0 )
						tempInode[i++] = pairs.getKey();
				}
				for(int tempI : tempInode) {
					victimDir.deleteFile(tempI);
				}
			}
			victimInode.releaseBlocks();
			FreeSpaceMgnt.consumeInode(victimInodeNo);
			this.dirContent.remove(victimInodeNo);
			this.writeToDisk();
		}
		else
			;//permission denied
	}
	
	public Inode makeDir(String dirName) {
		if(this.isWritable(new Inode(this.inodeNum))) {
			int inodeNum = FreeSpaceMgnt.getInode();
			Inode newDirInode = new Inode(inodeNum, TransSystem.getUser().getUserId(), TransSystem.getUser().getGrpId(), 6, 4, 4, 'd');
			String content = "d "+String.format("%03d", newDirInode.getInodeNum())+"\t.\nd "+String.format("%03d", this.inodeNum)+"\t..\n";
			newDirInode.writeContent(content);
			newDirInode.writeToDisk();
			dirName = this.cleanseName(dirName);
			DirEntry tempDirEntry = new DirEntry(dirName, 'd');
			this.dirContent.put(inodeNum, tempDirEntry);
			this.writeToDisk();
			return newDirInode;
		}
		else {
			//permission denied
			return null;
		}
	}
	
	private String cleanseName(String dirName) {
		boolean done = false, interrupted = false;
		int i = 1;
		while(!done) {
			interrupted = false;
			Iterator<Entry<Integer, DirEntry>> dirEntriesNavi = this.dirContent.entrySet().iterator();
			while(dirEntriesNavi.hasNext()) {
				Map.Entry<Integer, DirEntry> pairs = (Map.Entry<Integer, DirEntry>)dirEntriesNavi.next();
				DirEntry tempVal = (DirEntry) pairs.getValue();
				if(tempVal.getName().compareTo(dirName) == 0) {
					if(dirName.charAt(dirName.length() - 1) == ')')
						dirName = dirName.substring(0, dirName.length() - 3);
					dirName = dirName + "(" + i + ")";
					i++;
					interrupted = true;
				}
			}
			if(!interrupted)
				done = true;
		}
		return dirName;
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
		if(this.isReadable(new Inode(this.inodeNum))) {
			return this.dirContent;
		}
		else {
			//permission denied
			return null;
		}
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
		if(this.isWritable(new Inode(this.inodeNum))) {
			this.dirContent.remove(targetInodeNum);
			this.dirContent.put(targetInodeNum, new DirEntry(this.cleanseName(newFileName), 'd'));
			this.writeToDisk();
		}
		else
			;//permission denied
	}
	
	public void copy(int srcFileInode, int sourceDirInode) {
		Inode srcInode = new Inode(srcFileInode);
		Inode targetDirInode = new Inode(this.inodeNum);
		if(this.isReadable(srcInode) && this.isWritable(targetDirInode)) {
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
			DirEntry tempDirEntry = new DirEntry(this.cleanseName(srcEntry.getName()), srcEntry.getType());
			this.dirContent.put(targetInode.getInodeNum(), tempDirEntry);
			this.writeToDisk();
		}
		else
			;//ERROR: permission denied
	}
	/*
	 * Possible permissions:
	 * 4: read
	 * 2: write
	 * 1: execute???
	 * [User][Group][World]
	 * */
	private boolean isReadable(Inode srcInode) {
		boolean readable = false;
		int[] perm = srcInode.getPermissions();
		if(perm[2] >= 4)
			readable = true;
		else if(TransSystem.getUser().getGrpId() == srcInode.getGrpId()) {
			if(perm[1] >= 4)
				readable = true;
		}
		else if(TransSystem.getUser().getUserId() == srcInode.getUserId()) {
			if(perm[0] >= 4)
				readable = true;
		}
		return readable;
	}
	private boolean isWritable(Inode srcInode) {
		boolean readable = false;
		int[] perm = srcInode.getPermissions();
		if(perm[2] >= 6)
			readable = true;
		else if(TransSystem.getUser().getGrpId() == srcInode.getGrpId()) {
			if(perm[1] >= 6)
				readable = true;
		}
		else if(TransSystem.getUser().getUserId() == srcInode.getUserId()) {
			if(perm[0] >= 6)
				readable = true;
		}
		return readable;
	}
	
	public void move(int srcFileInode, int srcDirInode) {	//move and delete original entry
		Inode srcFileI = new Inode(srcFileInode);
		Inode srcDirI = new Inode(srcDirInode);
		Inode targetDirInode = new Inode(this.inodeNum);
		if(this.isReadable(srcFileI) && this.isWritable(srcDirI) && this.isWritable(targetDirInode)) {
			this.copy(srcFileInode, srcDirInode);
			Directory victimParentDir = new Directory(srcDirInode);
			victimParentDir.deleteFile(srcFileInode);
		}
		else
			;//permission denied
	}
	
	public void editFile(int fileInodeNum) {
		Inode fileInode = new Inode(fileInodeNum);
		if(this.isWritable(fileInode)) {
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
		else
			;//permission denied
	}
	public void chmod(int targetInodeNum, String permS) {
		Inode targetInode = new Inode(targetInodeNum);
		if(TransSystem.getUser().getUserId() == targetInode.getUserId()) {
			int[] perm = new int[3];
			for(int i = 0; i < 3; i++) {
				perm[i] = Integer.parseInt(permS.substring(i, i+1));
			}
			targetInode.setPermissions(perm);
		}
		else
			;//permission denied
	}
	public void chown(int targetInodeNum, String newUser) {
		Inode targetInode = new Inode(targetInodeNum);
		if(targetInode.getUserId() == targetInode.getUserId()) {
			int newUserId = User.getUserId(newUser);
			if(newUserId > 0) {
				targetInode.setUserId(newUserId);
			}
			else
				;//invalid user
		}
		else
			;//permission denied
	}
}