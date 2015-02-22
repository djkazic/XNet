package io;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import blocks.BlockedFile;
import main.Core;
import main.Utils;

public class FileWatcher implements Runnable {
	
	private WatchKey watchKey;
	
	public FileWatcher() {
		Path regDir = Paths.get(Utils.defineDir());
		try {
			WatchService fileWatcher = regDir.getFileSystem().newWatchService();
			watchKey = regDir.register(fileWatcher, 
							StandardWatchEventKinds.ENTRY_CREATE, 
							StandardWatchEventKinds.ENTRY_DELETE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utils.print(this, "FileWatcher registered to normal directory");
	}
	
	public void run() {
		while(true) {
			List<WatchEvent<?>> events = watchKey.pollEvents();
			for(WatchEvent<?> we : events) {
				if(we.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					System.out.println("Created: " + we.context().toString());
					new BlockedFile(Utils.defineDir() + "/" + we.context().toString());
				}
				if(we.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
					System.out.println("Deleted: " + we.context().toString());
					for(int i=0; i < Core.blockDex.size(); i++) {
						if(Core.blockDex.get(i).getName().equals(we.context().toString())) {
							Core.blockDex.remove(i);
						}
					}
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
