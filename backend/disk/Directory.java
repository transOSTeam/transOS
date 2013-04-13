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

import backend.InvalidUserException;
import backend.OperationNotPermittedException;
import backend.PermissionDeniedException;
import backend.TransSystem;
import backend.User;



//import backend.Directory.DirEntry;


public class Directory {
	private int inodeNum;
	//private int parentInodeNum;
	public static boolean godMode = false;
	private HashMap<Integer, DirEntry> dirContent;  
	
	public Directory(int inodeNum){
		this.inodeNum = inodeNum;
		dirContent = new HashMap<Integer, DirEntry>();
		Inode dirInode = new Inode(inodeNum);
		int[] blkPointers = dirInode.getBlockPointers();
		for(int i = 0; i < dirInode.getBlockCount(); i++) {
			try {
				Block tempBlk = new Block(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", blkPointers[i]) + ".txt", "r");
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
	
	public Inode makeFile(String fileName, String fileContent) throws PermissionDeniedException {
		if(Directory.isWritable(new Inode(this.inodeNum))) {
			int inodeNum = FreeSpaceMgnt.getInode();
			Inode newFileInode = new Inode(inodeNum, TransSystem.getUser().getUserId(), TransSystem.getUser().getGrpId(), 6, 4, 4, 'r');
			newFileInode.writeContent(fileContent);
			newFileInode.writeToDisk();
			DirEntry tempDirEntry = new DirEntry(this.cleanseName(fileName), 'r');
			this.dirContent.put(inodeNum, tempDirEntry);
			this.writeToDisk();
			return newFileInode;
		}
		else {
			throw new PermissionDeniedException();
		}
	}
	
	public void deleteFile(int victimInodeNo) throws PermissionDeniedException {
		Inode victimInode = new Inode(victimInodeNo);
		Inode victimsFolder = new Inode(this.inodeNum);
		if(Directory.isWritable(victimInode) && Directory.isWritable(victimsFolder)) {
			if(victimInode.getFileType() == 'd') {
				Directory victimDir = new Directory(victimInodeNo);
				int[] tempInode = new int[victimDir.dirContent.size() - 2];
				Iterator<Entry<Integer, DirEntry>> dirEntriesNavi = victimDir.dirContent.entrySet().iterator();
				int i = 0;
				while(dirEntriesNavi.hasNext()) {
					Map.Entry<Integer, DirEntry> pairs = (Map.Entry<Integer, DirEntry>)dirEntriesNavi.next();
					if(pairs.getValue().getName().compareTo(".") != 0 && pairs.getValue().getName().compareTo("..") != 0 ) {
						tempInode[i++] = pairs.getKey();
						System.out.println(pairs.getValue().getName());
					}
				}
				for(int tempI : tempInode) {
					victimDir.deleteFile(tempI);
				}
			}
			victimInode.hardLinkmm();			//hardLinkCount--
			victimInode.writeToDisk();
			if(victimInode.getHardLinkCount() == 0) {
				victimInode.releaseBlocks();
				FreeSpaceMgnt.consumeInode(victimInodeNo);
			}
			this.dirContent.remove(victimInodeNo);
			this.writeToDisk();
		}
		else
			throw new PermissionDeniedException();
	}
	
	public Inode makeDir(String dirName) throws PermissionDeniedException {
		if(Directory.isWritable(new Inode(this.inodeNum))) {
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
			throw new PermissionDeniedException();
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
	
	public HashMap<Integer, DirEntry> getDirContent() throws PermissionDeniedException{
		if(Directory.isReadable(new Inode(this.inodeNum))) {
			return this.dirContent;
		}
		else {
			throw new PermissionDeniedException();
		}
	}
	
	public int searchDir(String name) throws FileNotFoundException {				//linear search
		int inodeNum = 0;
		Iterator<Entry<Integer, DirEntry>> dirEntriesNavi = this.dirContent.entrySet().iterator();
		while(dirEntriesNavi.hasNext()) {
			Map.Entry<Integer, DirEntry> pairs = (Map.Entry<Integer, DirEntry>)dirEntriesNavi.next();
			if(pairs.getValue().getName().compareTo(name) == 0) {
				inodeNum = pairs.getKey();
				break;
			}
		}
		if(inodeNum == 0)
			throw new FileNotFoundException();
		return inodeNum;
	}
	public String searchDir(int inodeNum) throws FileNotFoundException {				//linear search
		String entryName = null;
		Iterator<Entry<Integer, DirEntry>> dirEntriesNavi = this.dirContent.entrySet().iterator();
		while(dirEntriesNavi.hasNext()) {
			Map.Entry<Integer, DirEntry> pairs = (Map.Entry<Integer, DirEntry>)dirEntriesNavi.next();
			if(pairs.getKey() == inodeNum) {
				entryName = pairs.getValue().getName();
				break;
			}
		}
		if(entryName == null)
			throw new FileNotFoundException();
		return entryName;
	}
	
	public void renameFile(int targetInodeNum, String newFileName) throws PermissionDeniedException {
		if(Directory.isWritable(new Inode(this.inodeNum))) {
			DirEntry tempEntry = this.dirContent.get(targetInodeNum);
			this.dirContent.remove(targetInodeNum);
			this.dirContent.put(targetInodeNum, new DirEntry(this.cleanseName(newFileName), tempEntry.getType()));
			this.writeToDisk();
		}
		else
			throw new PermissionDeniedException();
	}
	
	public void copy(int srcFileInode, int sourceDirInode) throws PermissionDeniedException {
		Inode srcInode = new Inode(srcFileInode);
		Inode targetDirInode = new Inode(this.inodeNum);
		if(Directory.isReadable(srcInode) && Directory.isWritable(targetDirInode)) {
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
			throw new PermissionDeniedException();
	}
	/*
	 * Possible permissions:
	 * 4: read
	 * 2: write
	 * 1: execute???
	 * [User][Group][World]
	 * */
	public static boolean isReadable(Inode srcInode) {
		if(godMode) {
			return true;
		}
		else {
			boolean readable = false;
			int[] perm = srcInode.getPermissions();
			if(perm[2] >= 4)
				readable = true;
			else if(perm[1] >= 4){
				if(TransSystem.getUser().getGrpId() == srcInode.getGrpId())
					readable = true;
			}
			else if(perm[0] >= 4) {
				if(TransSystem.getUser().getUserId() == srcInode.getUserId())
					readable = true;
			}
			return readable;
		}
	}
	public static boolean isWritable(Inode srcInode) {
		if(godMode) {
			return true;
		}
		else {
			boolean writable = false;
			int[] perm = srcInode.getPermissions();
			if(perm[2] >= 6)
				writable = true;
			else if(perm[1] >= 6) {
				if(TransSystem.getUser().getGrpId() == srcInode.getGrpId())
					writable = true;
			}
			else if(perm[0] >= 6) {
				if(TransSystem.getUser().getUserId() == srcInode.getUserId())
					writable = true;
			}
			return writable;
		}
	}
	
	public void move(int srcFileInode, int srcDirInode) throws PermissionDeniedException {	//move and delete original entry
		Inode srcFileI = new Inode(srcFileInode);
		Inode srcDirI = new Inode(srcDirInode);
		Inode targetDirInode = new Inode(this.inodeNum);
		if(Directory.isReadable(srcFileI) && Directory.isWritable(srcDirI) && Directory.isWritable(targetDirInode)) {
			this.copy(srcFileInode, srcDirInode);
			Directory victimParentDir = new Directory(srcDirInode);
			victimParentDir.deleteFile(srcFileInode);
		}
		else
			throw new PermissionDeniedException();
	}
	
	public void editFile(int fileInodeNum) throws PermissionDeniedException, FileNotFoundException {
		Inode fileInode = new Inode(fileInodeNum);
		if(Directory.isReadable(fileInode)) {
			if(fileInode.getFileType() == 'r') {
				File tempFile = null;
				String content = fileInode.getFileContent();
				try {
					tempFile = new File(Disk.tmpFolder.toString() + "/" + fileInodeNum + ".txt");
					if(tempFile.exists()) {
						tempFile.delete();
						tempFile.createNewFile();
					}
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
			throw new PermissionDeniedException();
	}
	public void chmod(int targetInodeNum, String permS) throws PermissionDeniedException {
		Inode targetInode = new Inode(targetInodeNum);
		if(TransSystem.getUser().getUserId() == targetInode.getUserId()) {
			int[] perm = new int[3];
			for(int i = 0; i < 3; i++) {
				perm[i] = Integer.parseInt(permS.substring(i, i+1));
			}
			targetInode.setPermissions(perm);
			targetInode.writeToDisk();
		}
		else
			throw new PermissionDeniedException();
	}
	public void chown(int targetInodeNum, String newUser) throws PermissionDeniedException, InvalidUserException {
		Inode targetInode = new Inode(targetInodeNum);
		if(targetInode.getUserId() == targetInode.getUserId()) {
			int newUserId = User.getUserId(newUser);
			if(newUserId > 0) {
				targetInode.setUserId(newUserId);
				targetInode.writeToDisk();
			}
			else
				throw new InvalidUserException();
		}
		else
			throw new PermissionDeniedException();
	}
	public void makeHardLink(String targetPath) throws FileNotFoundException, OperationNotPermittedException, PermissionDeniedException {
		Inode targetInode = this.parsePath(targetPath);
		
		String[] splitPath = targetPath.split("/");
		String name = splitPath[splitPath.length - 1];
		try {
			this.searchDir(name);
			throw new OperationNotPermittedException();
		} catch(FileNotFoundException e) {
			if(Directory.isWritable(new Inode(this.inodeNum))) {
				targetInode.hardLinkpp();				//hardLinkCount++;
				targetInode.writeToDisk();
				this.dirContent.put(targetInode.getInodeNum(), new DirEntry(name, targetInode.getFileType()));
				this.writeToDisk();
			}
			else
				throw new PermissionDeniedException();
		}
	}

	private Inode parsePath(String targetPath) throws FileNotFoundException, PermissionDeniedException{
		String[] splitPath = targetPath.split("/");
		Inode tempInode = new Inode(2);
		Directory tempDir = new Directory(2);
		for( String temp : splitPath) {
			if(temp.compareTo("") != 0) {
				if(Directory.isReadable(tempInode)) {
					tempInode = new Inode(tempDir.searchDir(temp));
					if(tempInode.getFileType() == 'd')
						tempDir = new Directory(tempInode.getInodeNum());
				}
				else
					throw new PermissionDeniedException();
			}
		}
		return tempInode;
	}
	public Inode makeSoftLink(String targetPath) throws FileNotFoundException, PermissionDeniedException {
		Inode softLinkInode = null;
		if(Directory.isWritable(new Inode(this.inodeNum))) {
			String[] pathSplit = targetPath.split("/");
			String fileName = pathSplit[pathSplit.length - 1];
			softLinkInode = this.makeFile("shortcut to " + fileName, targetPath);
			softLinkInode.setFileType('s');
			softLinkInode.writeToDisk();
			DirEntry oldSoftLinkEntry = this.dirContent.remove(softLinkInode.getInodeNum());
			this.dirContent.put(softLinkInode.getInodeNum(), new DirEntry(oldSoftLinkEntry.getName(), 's'));
			this.writeToDisk();
		}
		else
			throw new PermissionDeniedException();
		return softLinkInode;
	}
	public Inode openSoftLink(int linkInodeNum) throws FileNotFoundException, PermissionDeniedException {			//return targetFile inode: test if dir or file and then do the necessary
		Inode linkInode = new Inode(linkInodeNum);
		String filePath = linkInode.getFileContent();
		Inode fileInode = this.parsePath(filePath);
		return fileInode;
	}
}