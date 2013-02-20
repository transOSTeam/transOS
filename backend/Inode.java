package backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	
	//private Block[] diskAddress;
	
	Inode(){
		inodeNumber = 0;
		refCount = 0;
		hardLinkCount = 0;
		userId = 0;
		grpId = 0;
		java.util.Date date= new java.util.Date();
		createdTime = modifyTime = accessedTime = new Timestamp(date.getTime());
		blockCount = 0;
		permission = new int[]{7,7,7};
		fileType = '-';
		active = true;
	}

	public void writeToFile(File f) {
		long fileSize = f.length();
		String content = this.blockCount+"\n"+this.fileType+"\n"+this.grpId+"\n"+this.hardLinkCount+"\n"+this.inodeNumber+"\n"+this.refCount+"\n"+this.userId+"\n"+this.accessedTime+"\n"+this.active+"\n"+this.createdTime+"\n"+this.modifyTime+"\n"+this.permission[0]+this.permission[1]+this.permission[2];
		while(fileSize <= kickStart.maxBlockSize - content.length()) {
			fileSize += content.length();
			FileWriter fw;
			try {
				fw = new FileWriter(f.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(content);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}