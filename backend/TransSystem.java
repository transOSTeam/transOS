package backend;

import java.io.File;
import backend.disk.Disk;
import backend.disk.DiskWatcher;

import frontend.GuiStarter;


public class TransSystem {

	private static User loggedInUser;
	
	public static void main(String[] args) {
		TransSystem.chkDisk();
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
	public static boolean authenticateUser(String username, String pswdHash) {
		boolean success = false;
		User tempUser = User.authenticate(username, pswdHash);
		if(tempUser != null) {
			loggedInUser = tempUser;
			success = true;
		}
		return success;
	}
	public static boolean registerUser(String username, String pswd, String grpName) {
		boolean success = false;
		User newUser = User.createNewUser(username, pswd, grpName);
		if(newUser != null) {
			loggedInUser = newUser;
			success = true;
		}
		return success;
	}
	public static User getUser() {
		return loggedInUser;
	}
	public static void setUser(User newUser) {
		TransSystem.loggedInUser = newUser;
	}
}
