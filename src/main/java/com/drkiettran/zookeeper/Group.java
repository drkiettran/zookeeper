package com.drkiettran.zookeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

public class Group implements Watcher {
	private static final int SESSION_TIMEOUT = 5000;
	private ZooKeeper zk;
	private CountDownLatch connectedSignal = new CountDownLatch(1);

	public static Group getInstance() {
		return new Group();
	}

	public void connect(String hosts) throws IOException, InterruptedException {
		zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
		connectedSignal.await();
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getState() == Event.KeeperState.SyncConnected) {
			connectedSignal.countDown();
		}
	}

	public void create(String groupName) throws KeeperException, InterruptedException {
		String path = "/" + groupName;
		String createdPath = zk.create(path, null/* data */, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println("Created " + createdPath);
	}

	public List<String> list(String groupName) throws KeeperException, InterruptedException {
		String path = "/" + groupName;
		try {
			return zk.getChildren(path, false);
		} catch (KeeperException.NoNodeException e) {
			System.out.printf("Group %s does not exist\n", groupName);
		}
		return new ArrayList<>();
	}

	public void delete(String groupName) throws KeeperException, InterruptedException {
		String path = "/" + groupName;
		try {
			List<String> children = zk.getChildren(path, false);
			for (String child : children) {
				zk.delete(path + "/" + child, -1);
			}
			zk.delete(path, -1);
		} catch (KeeperException.NoNodeException e) {
			System.out.printf("Group %s does not exist\n", groupName);
		}
	}

	public void join(String groupName, String memberName) throws KeeperException, InterruptedException {
		String path = "/" + groupName + "/" + memberName;
		String createdPath = zk.create(path, null/* data */, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		System.out.println("Created " + createdPath);
	}

	public boolean exists(String groupName) throws KeeperException, InterruptedException {
		String path = "/" + groupName;
		return zk.exists(path, false) != null;
	}

	public void close() throws InterruptedException {
		zk.close();
	}

	public static void main(String[] argv) throws IOException, InterruptedException, KeeperException {
		String host = argv[0];
		String groupName = argv[1];
		String member1 = argv[2];
		String member2 = argv[3];

		Group group = Group.getInstance();
		group.connect(host);
		if (group.exists(groupName)) {
			group.delete(groupName);
		}
		group.create(groupName);
		group.join(groupName, member1);
		group.join(groupName, member2);

		List<String> list = group.list(groupName);
		list.stream().forEach(node -> {
			System.out.println(node);
		});

		group.close();
	}
}
