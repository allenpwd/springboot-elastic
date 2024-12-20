## Elasticsearch简介
Elasticsearch是一个分布式搜索服务，提供Restful API，底层基于Lucene，采用多shard（分片）的方式保证数据安全，\
并且提供自动resharding的功能，github等大型的站点也是采用了ElasticSearch作为其搜索服务

### docker安装elasticsearch
```docker
docker run -e ES_JAVA_OPTS="-Xms256m -Xmx256m" -d -p 9200:9200 -p 9300:9300 --name myES <IMAGEID>
```

### 问题
#### elasticsearch版本兼容问题
    spring-data-elasticsearch和elasticsearch的版本对应关系可以看github上的项目说明：https://github.com/spring-projects/spring-data-elasticsearch
    这里用的spring-boot版本为2.1.0，elasticsearch需要6.2.2以上
    
#### elasticsearch使用docker启动失败
    docker logs <containerId>查看出错日志，发现报错：max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
    意思是最大虚拟内存区 vm.max_map_count 设置的 65530 太低，至少要增加到至少262144
    可以执行命令：sudo sysctl -w vm.max_map_count=262144
    解决方式：https://blog.csdn.net/qq_43268365/article/details/88234308
    
#### NoNodeAvailableException[None of the configured nodes are available
    cluster-name和cluster-nodes要配置正确
    
    
### 注意
Elasticsearch从7版本开始TransportClient已经过时了不再推荐使用，将在8.0版本删除，具体参考https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html。\
Spring Data Elasticsearch支持TransportClient，只要它在已使用的Elasticsearch版本中可用，但从4.0版本起已经不再建议使用它。transportClient要客户端和服务端版本完全匹配的，后续想升级es，很麻烦\
用http请求就没有这个问题，官方封装了high level rest client
