package backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Block extends RandomAccessFile{

	public Block(String name, String mode) throws FileNotFoundException {
		super(name, mode);
		
	}
	
}
