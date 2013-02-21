package backend;

import java.io.File;

import frontend.GuiStarter;


public class kickStart {

	public static final int maxBlockSize = 500;	// in B
	public static final int diskSize = 1;			// in MB
	
	public static void main(String[] args) {
		kickStart.chkDisk();
		//start GUI
		new GuiStarter();
	}
	
	private static void chkDisk(){
		File disk = Disk.transDisk;
		
		if(!disk.exists()){
			Disk.createDisk();
		}
	}
}
