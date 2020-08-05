package com.drkiettran.zookeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public class SetContent implements Watcher {
	private static final int SESSION_TIMEOUT = 5000;
	private ZooKeeper zk;
	private CountDownLatch connectedSignal = new CountDownLatch(1);

	public void connect(String hosts) throws IOException, InterruptedException {
		zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
		connectedSignal.await();
	}

	@Override
	public void process(WatchedEvent event) { // Watcher interface
		if (event.getState() == KeeperState.SyncConnected) {
			connectedSignal.countDown();
		}
	}

	public void set(String groupName, String data) throws KeeperException, InterruptedException {
		String path = groupName.charAt(0) == '/' ? "" : "/";
		path += groupName;

		try {
			zk.setData(path, data.getBytes(), 0);
			System.out.printf("Stored %s into %s ", data, path);
		} catch (KeeperException.NodeExistsException e) {
			System.out.printf("ERROR: Group %s does not exist\n", groupName);
		} catch (KeeperException.BadVersionException e) {
			System.out.printf("ERROR: bad data version!\n", groupName);
		}
	}

	public void close() throws InterruptedException {
		zk.close();
	}

	public static void main(String[] args) throws Exception {
		SetContent sc = new SetContent();
		sc.connect(args[0]);
		sc.set(args[1], args[2]);
		sc.close();
	}
}
