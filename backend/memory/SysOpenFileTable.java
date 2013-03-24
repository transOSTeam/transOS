package backend.memory;

import backend.disk.Directory;
import backend.disk.Inode;

public class SysOpenFileTable {
	private static class SysOpenFileTableEntry{
		Inode openFileInode;
		int processCount;
		
		public SysOpenFileTableEntry(Inode entryInode) {
			this.openFileInode = entryInode;
			this.processCount = 0;
		}
	}
	
	final private static int tableSize = 100;
	private static SysOpenFileTableEntry[] entries = new SysOpenFileTableEntry[tableSize];
	private static int usedBuckets = 0;
	private static boolean[] usageBitmap = new boolean[tableSize];
	
	public static boolean isFull() {
		if(usedBuckets == tableSize)
			return true;
		else
			return false;
	}
	
	public static void addEntry(String path) {
		int inodeNum = SysOpenFileTable.parsePath(path);
		boolean done = false;
		for(int i = 0; i < SysOpenFileTable.usedBuckets; i++) {
			if(SysOpenFileTable.usageBitmap[i] && SysOpenFileTable.entries[i].openFileInode.getInodeNum() == inodeNum) {
				SysOpenFileTable.entries[i].processCount++;
				done = true;
				break;
			}
		}
		if(!done) {
			Inode newEntryInode = new Inode(inodeNum);
			SysOpenFileTable.entries[SysOpenFileTable.getBucket()] = new SysOpenFileTableEntry(newEntryInode);
		}
	}
	private static int parsePath(String path) {
		String[] dirs = path.split("/");
		/* Search in "/"(root) since it won't be split; 
		 * Do we need / in table...becoz we anyways know its inode number?*/
		int inodeNum = 2;
		for(int i = 0; i < dirs.length; i++) {
			Directory rootDir = new Directory(inodeNum);
			inodeNum = rootDir.searchDir(dirs[i]);
		}
		return inodeNum;
	}
	private static int getBucket() {
		int index;
		for(index = 0; index < SysOpenFileTable.tableSize && !SysOpenFileTable.usageBitmap[index]; index++)
			;
		if(index < SysOpenFileTable.tableSize) {
			SysOpenFileTable.usageBitmap[index] = true;
			SysOpenFileTable.usedBuckets++;
			return index;
		}
		else								//throw exception
			return -1;
	}
}
