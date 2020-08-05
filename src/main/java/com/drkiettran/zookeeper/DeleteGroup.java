package com.drkiettran.zookeeper;

import java.util.List;

import org.apache.zookeeper.KeeperException;

public class DeleteGroup extends ConnectionWatcher {
	public void delete(String groupName) throws KeeperException, InterruptedException {
		String path = groupName.charAt(0) == '/' ? "" : "/";
		path += groupName;

		try {
			List<String> children = zk.getChildren(path, false);
			for (String child : children) {
				zk.delete(path + "/" + child, -1);
			}
			zk.delete(path, -1);
			System.out.printf("Group %s deleted!", groupName);
		} catch (KeeperException.NoNodeException e) {
			System.out.printf("Group %s does not exist\n", groupName);
		}
	}

	public static void main(String[] args) throws Exception {
		DeleteGroup deleteGroup = new DeleteGroup();
		deleteGroup.connect(args[0]);
		deleteGroup.delete(args[1]);
		deleteGroup.close();
	}
}