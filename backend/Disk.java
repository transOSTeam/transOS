package backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Disk {
	
	public static final File homeDir = new File(System.getProperty("user.home"));	
	public static final File transDisk = new File(homeDir.toString() + "/TransDisk");
	private static final int inodeStartBlock = 5;
	private static final int inodeEndBlock = 50;
	
	private static final int partitionTableAddress = 0;
	/*
	public Disk(){
		createRoot();
		createBlocks();
	}*/
	public static void createDisk(){
		createRoot();
		createBlocks();
		makePartitionTable();
		initializeInodes();
	}
	
	private static void initializeInodes() {
		for(int i = inodeStartBlock; i <= inodeEndBlock; i++){
			File f = new File(homeDir.toString() + "/TransDisk/" + String.format("%05d", i));
			if(!f.exists()){
				System.out.println("Fatal Error...block " +i+ " not present");
			}
			else{
				Inode test = new Inode();
				test.writeToFile(f);
			}
		}
	}

	private static void createRoot(){
		if(!transDisk.exists()){
			if(transDisk.mkdir()){
				System.out.println("root created");
			}
			else{
				System.out.println("root creation failed");
			}
		}
		else{
			System.out.println(transDisk.length());
			System.out.println("root dir present");
		}
	}
	
	private static void createBlocks(){
		int noOfBlocks = kickStart.diskSize*1000*1000/kickStart.maxBlockSize;
		for(int i = 0; i < noOfBlocks; i++){
			File f = new File(homeDir.toString() + "/TransDisk/" + String.format("%05d", i));
			if(!f.exists()){
				try {
					f.createNewFile();	
					System.out.println("\t"+i+ " created");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				System.out.println("block present");
			}
		}
	}
	private static void makePartitionTable(){
		try {
			String content;// = "//Partition Table\n//Status\tStart\tEnd";			//4th entry omitted
			
			File partitionTable = new File(Disk.transDisk + "/" +String.format("%05d", Disk.partitionTableAddress));
 
			if (!partitionTable.exists()) {
				System.out.println("Partition Table Not found");
			}
			
			//_________Hard coding partition___________________
			content = "11111\t" + String.format("%05d", 1) + "\t" + String.format("%05d", 10);
			
			FileWriter fw = new FileWriter(partitionTable.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
