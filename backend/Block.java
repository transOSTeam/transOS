package backend;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class Block extends RandomAccessFile{

	private int blockNumber;
	
	public Block(String name, String mode) throws FileNotFoundException {
		super(name, mode);
		this.blockNumber = Integer.parseInt(name.substring(name.length()-3, name.length()));
	}
	public int getBlockNumber(){
		return this.blockNumber;
	}
	
}
