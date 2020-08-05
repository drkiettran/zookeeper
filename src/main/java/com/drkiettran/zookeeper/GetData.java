package com.drkiettran.zookeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class GetData implements Watcher {
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

	public void set(String groupName) throws KeeperException, InterruptedException {
		String path = groupName.charAt(0) == '/' ? "" : "/";
		path += groupName;

		try {
			Stat stat = new Stat();
			String data = new String(zk.getData(path, false, stat));
			if (data != null) {
				System.out.printf("Got `%s` from %s\n", data, path);
				System.out.printf("version %d\n", stat.getVersion());
			}
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
		GetData gd = new GetData();
		gd.connect(args[0]);
		gd.set(args[1]);
		gd.close();
	}
}
