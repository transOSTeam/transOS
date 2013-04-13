package backend.disk;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DiskWatcher implements Runnable{
	final FileSystem fs = FileSystems.getDefault();
	
	public void run() {
		WatchService ws;
		try {
			ws = fs.newWatchService();
			Path pth = Paths.get(Disk.tmpFolder.getAbsolutePath());
			pth.register(ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			WatchKey wk = null;
			WatchEvent<?> lastEvent = null;
			do {
					wk = ws.take();
					for(WatchEvent<?> event : wk.pollEvents()) {
						lastEvent = event;
					}
					WatchEvent.Kind<?> kind = lastEvent.kind();
					System.out.println(kind);
					Path eventPath = (Path) lastEvent.context();
					String fileName = eventPath.getFileName().toString();
					if(fileName.substring(fileName.length() - 4, fileName.length()).compareTo(".txt") == 0) {
						File f = new File(Disk.tmpFolder.toString() + "/" + eventPath.getFileName());
						RandomAccessFile tempRF = new RandomAccessFile(f, "r");
						String newContent = "", buffer;
						while((buffer = tempRF.readLine()) != null) {
							newContent += buffer + "\n";
						}
						String temp = eventPath.getFileName().toString();
						Inode fileInode = new Inode(Integer.parseInt(temp.substring(0, temp.length() - 4)));
						if(Directory.isWritable(fileInode)) {
							fileInode.writeContent(newContent);
							fileInode.writeToDisk();
						}
						tempRF.close();
					}
			} while(wk.reset());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
