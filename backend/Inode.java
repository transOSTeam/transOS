package backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class Inode {
	private int signature;					// 0 inactive, 1 Inode, 2 single indirect data block, 3 double indirect and so on...
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
	private int[] blockPointers;
	
	Inode(){
		signature = 0;
		inodeNumber = 0;
		refCount = 0;
		hardLinkCount = 0;
		userId = 0;
		grpId = 0;
		java.util.Date date= new java.util.Date();
		createdTime = modifyTime = accessedTime = new Timestamp(date.getTime());
		blockCount = 0;
		permission = new int[]{7,7,7};
		fileType = 'r';
		blockPointers = new int[] {0,0,0,0,0};
	}

	public void writeToFile(File f) {
		long fileSize = f.length();
		boolean firstWrite = false;
		
		String content = this.signature+String.format("%02d", this.blockCount)+"\n"+this.fileType+"\n"+String.format("%03d", this.grpId)+"\n"+String.format("%02d", this.hardLinkCount);
		content += "\n"+String.format("%05d", this.inodeNumber)+"\n"+String.format("%02d", this.refCount)+"\n"+String.format("%03d", this.userId)+"\n";
		content += this.accessedTime+"\n"+this.createdTime+"\n"+this.modifyTime+"\n"+this.permission[0]+this.permission[1]+this.permission[2]+"\n"+String.format("%05d", this.blockPointers[0]);
		content += "\n"+String.format("%05d", this.blockPointers[1])+"\n"+String.format("%05d", this.blockPointers[2])+"\n"+String.format("%05d", this.blockPointers[3])+"\n"+String.format("%05d", this.blockPointers[4]);
		
		while(fileSize <= kickStart.maxBlockSize - content.length()) {
			fileSize += content.length();
			FileWriter fw;
			try {
				fw = new FileWriter(f.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				if(firstWrite == false) {
					bw.write(content);
					firstWrite = true;
				}
				else
					bw.write("\n"+content);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(content.length());
	}
}