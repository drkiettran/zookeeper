# Zookeeper sample project
In this project, I am using Java code for ZooKeep znode related APIs from
`Hadoop: A Definitive Guide` by White (2015).

## Build

```shell script
mvn clean package
```

## Run
```bash
java -cp ./target/zookeeper-jar-with-dependencies.jar com.drkiettran.zookeeper.CreateGroup localhost zoo
```
