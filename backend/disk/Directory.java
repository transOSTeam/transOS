package backend.disk;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;



//import backend.Directory.DirEntry;


public class Directory {
	private static int tempNo;
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
		victimInode.releaseBlocks();
		FreeSpaceMgnt.consumeInode(victimInodeNo);
		this.dirContent.remove(victimInodeNo);
		/*Inode thisInode = new Inode(this.inodeNum);
		thisInode.releaseBlocks();
		FreeSpaceMgnt.consumeInode(this.inodeNum);*/
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
	
	public void copy(int srcFileInode, int targetDirInode) {
		Inode victimInode = new Inode(srcFileInode);
		Inode targetInode = new Inode(victimInode);
		DirEntry victimEntry = this.dirContent.get(srcFileInode);
		Directory targetDir = new Directory(targetDirInode);
		DirEntry tempDirEntry = new DirEntry(victimEntry.getName(), victimEntry.getType());
		targetDir.dirContent.put(targetInode.getInodeNum(), tempDirEntry);
		targetDir.writeToDisk();
	}
	public void move(int scrFileInode, int targetDirInode) {	//just cut the entry from source to target directory
		DirEntry victimEntry = this.dirContent.remove(scrFileInode);
		Directory targetDir = new Directory(targetDirInode);
		targetDir.dirContent.put(scrFileInode, victimEntry);
		
		this.writeToDisk();
		targetDir.writeToDisk();
	}
	
	public void editFile(int fileInodeNum) {
		Inode fileInode = new Inode(fileInodeNum);
		if(fileInode.getFileType() == 'r') {
			File tempFile = null;
			String content = fileInode.getFileContent();
			try {
				String tempNoS = ""+tempNo;
				tempFile = new File(Disk.homeDir.toString() + "/transOStempFile" + tempNoS);
				tempNo++;
				tempFile.deleteOnExit();
				RandomAccessFile tempRF = new RandomAccessFile(tempFile, "rw");
				tempRF.writeBytes(content);
				Desktop.getDesktop().open(tempFile);
				//do stuff here...copy back to inode.writeContent();
				tempRF.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			final FileSystem fs = FileSystems.getDefault();
			try {
				WatchService ws = fs.newWatchService();
				Path pth = Paths.get(tempFile.getAbsoluteFile().getParentFile().getAbsolutePath());
				pth.register(ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
				boolean loop = true;
				WatchKey wk = null;
				do {
					try {
						wk = ws.take();
						for(WatchEvent<?> event : wk.pollEvents()) {
							WatchEvent.Kind<?> kind = event.kind();
							Path eventPath = (Path) event.context();
							System.out.println(eventPath.getFileName() + "->" + kind.toString());
							if(eventPath.getFileName().toString().compareTo(tempFile.getName()) == 0) {
								loop = false;
							}
							wk.reset();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while(loop);
				RandomAccessFile tempRF = new RandomAccessFile(tempFile, "r");
				String newContent = "", buffer;
				while((buffer = tempRF.readLine()) != null) {
					newContent += buffer;
				}
				fileInode.writeContent(newContent);
				fileInode.writeToDisk();
				tempRF.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}