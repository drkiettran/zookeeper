package com.drkiettran.zookeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class CreateGroup implements Watcher {
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

	public void create(String groupName) throws KeeperException, InterruptedException {
		String path = groupName.charAt(0) == '/' ? "" : "/";
		path += groupName;
		String createdPath;
		try {
			createdPath = zk.create(path, null/* data */, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			System.out.println("Created " + createdPath);
		} catch (KeeperException.NodeExistsException e) {
			System.out.printf("ERROR: Group %s exists\n", groupName);
		}
	}

	public void close() throws InterruptedException {
		zk.close();
	}

	public static void main(String[] args) throws Exception {
		CreateGroup createGroup = new CreateGroup();
		createGroup.connect(args[0]);
		createGroup.create(args[1]);
		createGroup.close();
	}
}