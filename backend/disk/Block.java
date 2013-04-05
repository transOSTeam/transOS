package backend.disk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class Block extends RandomAccessFile{

	private int blockNumber;
	
	public Block(String name, String mode) throws FileNotFoundException {
		super(name, mode);
		this.blockNumber = Integer.parseInt(name.substring(name.length()-5, name.length()));
	}
	public Block(int blockNum, String mode) throws FileNotFoundException {
		super(Disk.homeDir.toString() + "/TransDisk/" + String.format("%05d", blockNum) + ".txt", mode);
		this.blockNumber = blockNum;
	}
	public int getBlockNumber(){
		return this.blockNumber;
	}
	public String getContent() {
		String content = "", buffer;
		try {
			while((buffer = this.readLine()) != null) {
				content += buffer + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content.trim();
	}
}
