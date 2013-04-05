package backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import backend.disk.Directory;
import backend.disk.Disk;
import backend.disk.Inode;

public class User {
	private String username;
	private String grpName;
	
	private int userId;
	private int grpId;
	private int homeDirInodeNum;
	
	private static final String pswdFilePath = Disk.transDisk.toString() + "/pswd";
	private static final String grpListPath = Disk.transDisk.toString() + "/groupList";
	
	private User(String username, String grpName, int userId, int grpId) {
		this.username = username;
		this.grpName = grpName;
		this.userId = userId;
		this.grpId = grpId;
	}
	
	public static User createNewUser(String newUsername, String password, String grpName) {
		User newUser = null;
		boolean noProblem = true;
		int entriesRead = 1;
		try {
			RandomAccessFile pswdF = new RandomAccessFile(User.pswdFilePath, "rw");
			String buffer;
			while((buffer = pswdF.readLine()) != null ) {
				entriesRead++;
				String[] splitBuffer = buffer.split("\t");
				if(splitBuffer[0].compareTo(newUsername) == 0) {
					noProblem = false;
					break;
				}
			}
			if(noProblem) {
				int grpId = calculateGrpId(grpName);
				newUser = new User(newUsername, grpName, entriesRead, grpId);
				TransSystem.setUser(newUser);
				Directory.godMode = true;
				Directory rootDir = new Directory(2);
				Inode homeDirInode;
				try {
					homeDirInode = rootDir.makeDir(newUsername);
					newUser.homeDirInodeNum = homeDirInode.getInodeNum();
				} catch (PermissionDeniedException e) {
					e.printStackTrace();
				}
				Directory.godMode = false;
				
				pswdF.writeBytes(newUsername + "\t" + User.hashIt(password) + "\t" + entriesRead + "\t" + grpId + "\t" + newUser.homeDirInodeNum +"\n");
			}
			pswdF.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newUser;
	}
	
	private static int calculateGrpId(String grpName) {
		int grpId = 0, entriesRead = 1;
		try {
			RandomAccessFile grpListFile = new RandomAccessFile(User.grpListPath, "rw");
			String buffer;
			boolean found = false;
			while(!found && (buffer = grpListFile.readLine()) != null) {
				entriesRead++;
				String[] splitBuffer = buffer.split("\t");
				if(splitBuffer[1].compareTo(grpName) == 0) {
					grpId = Integer.parseInt(splitBuffer[0]);
					found = true;
				}
			}
			if(!found) {
				grpListFile.writeBytes(entriesRead + "\t" + grpName + "\n");
				grpId = entriesRead;
			}
			grpListFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return grpId;
	}

	public static String hashIt(String input) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		m.reset();
		m.update(input.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 ){
		  hashtext = "0"+hashtext;
		}
		return hashtext;
	}

	public String getUsername() {
		return this.username;
	}
	public String getGrpName() {
		return this.grpName;
	}
	public int getUserId() {
		return this.userId;
	}
	public int getGrpId() {
		return this.grpId;
	}
	
	public static void initUserMgnt() {
		File pswdFile = new File(User.pswdFilePath);
		File grpListFile = new File(User.grpListPath);
		try {
			if(!pswdFile.exists())
				pswdFile.createNewFile();
			
			if(!grpListFile.exists())
				grpListFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static User authenticate(String username, String pswd) {
		User authenticatedUser = null;
		boolean authenticated = false;
		try {
			RandomAccessFile pswdFile = new RandomAccessFile(User.pswdFilePath, "r");
			String buffer;
			while((buffer = pswdFile.readLine()) != null && !authenticated) {
				String[] splitBuffer = buffer.split("\t");
				if(splitBuffer[0].compareTo(username) == 0) {
					if(splitBuffer[1].compareTo(User.hashIt(pswd)) == 0) {
						authenticatedUser = new User(username, User.getGrpName(splitBuffer[3]), Integer.parseInt(splitBuffer[2]), Integer.parseInt(splitBuffer[3]));
						authenticated = true;
						authenticatedUser.homeDirInodeNum = Integer.parseInt(splitBuffer[4]);
					}
					else
						break;					// wrong password
				}
			}
			pswdFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return authenticatedUser;
	}

	private static String getGrpName(String grpId) {
		boolean done = false;
		String grpName = null;
		try {
			RandomAccessFile grpList = new RandomAccessFile(User.grpListPath, "r");
			String buffer;
			while((buffer = grpList.readLine()) != null && !done) {
				String[] splitBuffer = buffer.split("\t");
				if(splitBuffer[1].compareTo(grpId) == 0) {
					grpName = splitBuffer[0];
					done = true;
				}
			}
			grpList.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return grpName;
	}
	public static int getUserId(String name) {
		boolean done = false;
		int userId = 0;
		try {
			RandomAccessFile pswdF = new RandomAccessFile(User.pswdFilePath, "r");
			String buffer;
			while((buffer = pswdF.readLine()) != null && !done) {
				String[] splitBuffer = buffer.split("\t");
				if(splitBuffer[0].compareTo(name) == 0) {
					userId = Integer.parseInt(splitBuffer[2]);
					done = true;
				}
			}
			pswdF.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return userId;
	}

	public int getHomeDirInodeNum() {
		return this.homeDirInodeNum;
	}
}
