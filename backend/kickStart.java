package backend;

import java.io.File;

import backend.disk.Disk;
import backend.disk.DiskWatcher;

import frontend.GuiStarter;

public class kickStart {

	
	public static void main(String[] args) {
		kickStart.chkDisk();
		Disk.bootUp();
		//start GUI
		DiskWatcher dw = new DiskWatcher();
		Thread watcher = new Thread(dw);
		watcher.start();
		new GuiStarter();
	}

	private static void chkDisk(){
		File disk = Disk.transDisk;

		if(!disk.exists()){
			Disk.createDisk();
		}
	}
}
