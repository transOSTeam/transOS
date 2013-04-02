package backend;

import java.io.File;

<<<<<<< HEAD
import backend.disk.Disk;
import backend.disk.DiskWatcher;

import frontend.GuiStarter;
=======
import frontend.GuiStarter;

>>>>>>> d38edc6b5f6a7659ef9572093ef7002dc0183fb1

public class kickStart {

	
	public static void main(String[] args) {
		kickStart.chkDisk();
		Disk.bootUp();
		//start GUI
<<<<<<< HEAD
		DiskWatcher dw = new DiskWatcher();
		Thread watcher = new Thread(dw);
		watcher.start();
=======
>>>>>>> d38edc6b5f6a7659ef9572093ef7002dc0183fb1
		new GuiStarter();
	}

	private static void chkDisk(){
		File disk = Disk.transDisk;

		if(!disk.exists()){
			Disk.createDisk();
		}
	}
}
