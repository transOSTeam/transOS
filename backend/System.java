package backend;

import java.io.File;
import backend.disk.Disk;
import backend.disk.DiskWatcher;

import frontend.GuiStarter;


public class System {

	
	public static void main(String[] args) {
		System.chkDisk();
		Disk.bootUp();
		DiskWatcher dw = new DiskWatcher();
		Thread watcher = new Thread(dw);
		watcher.start();
		new GuiStarter();
	}

	private static void chkDisk(){
		File disk = Disk.transDisk;

		if(!disk.exists()){
			Disk.createDisk();
			Disk.shutDown();
		}
	}
}
