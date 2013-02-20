package backend;

import java.sql.Timestamp;

public class Inode {
	private int inodeNumber;
	private int refCount;
	private int hardLinkCount;
	private int userId;
	private int grpId;
	private Timestamp accessedTime;
	private Timestamp modifyTime;
	private Timestamp createdTime;
	private int blockCount;
	private int permission[];
	private char fileType;
	private boolean active;
	
	private Block[] diskAddress;
}