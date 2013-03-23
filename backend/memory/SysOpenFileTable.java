package backend.memory;

import backend.disk.Inode;

public class SysOpenFileTable {
	private class SysOpenFileTableEntry{
		Inode openFileInode;
		int processCount;
		
		public SysOpenFileTableEntry() {
			this.processCount = 0;
		}
	}
	
	final private static int tableSize = 100;
	private static SysOpenFileTableEntry[] entries = new SysOpenFileTableEntry[tableSize];
	private static int usedBuckets = 0;
	private static byte[] usageBitmap = new byte[tableSize];
	
	public static boolean isFull() {
		if(usedBuckets == tableSize)
			return true;
		else
			return false;
	}
	
	public static void addEntry(String path) {
		String[] directory = path.split("/");
	}
}
