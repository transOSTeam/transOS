package backend.disk;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

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
			boolean loop = true;
			WatchKey wk = null;
			do {
					wk = ws.take();
					for(WatchEvent<?> event : wk.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();
						Path eventPath = (Path) event.context();
						System.out.println(eventPath.getFileName() + "->" + kind.toString());
						String fileName = eventPath.getFileName().toString();
						if(fileName.substring(fileName.length() - 4, fileName.length()).compareTo(".txt") == 0) {
								RandomAccessFile tempRF = new RandomAccessFile(Disk.tmpFolder.toString() + "/" + eventPath.getFileName(), "r");
								String newContent = "", buffer;
								while((buffer = tempRF.readLine()) != null) {
									newContent += buffer + "\n";
								}
								String temp = eventPath.getFileName().toString();
								Inode fileInode = new Inode(Integer.parseInt(temp.substring(0, temp.length() - 4)));
								fileInode.writeContent(newContent);
								fileInode.writeToDisk();
								tempRF.close();
							}
						}
					wk.reset();
			} while(loop);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
