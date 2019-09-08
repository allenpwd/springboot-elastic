##Elasticsearch简介
Elasticsearch是一个分布式搜索服务，提供Restful API，底层基于Lucene，采用多shard（分片）的方式保证数据安全，\
并且提供自动resharding的功能，github等大型的站点也是采用了ElasticSearch作为其搜索服务

###docker安装elasticsearch
- docker run -e ES_JAVA_OPTS="-Xms256m -Xmx256m" -d -p 9200:9200 -p 9300:9300 --name myES <IMAGEID>

###问题
####elasticsearch版本兼容问题
    spring-data-elasticsearch和elasticsearch的版本对应关系可以看github上的项目说明：https://github.com/spring-projects/spring-data-elasticsearch
    这里用的spring-boot版本为2.1.0，elasticsearch需要6.2.2以上
####elasticsearch使用docker启动失败
    docker logs <containerId>查看出错日志，发现报错：max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
    意思是最大虚拟内存区 vm.max_map_count 设置的 65530 太低，至少要增加到至少262144
    可以执行命令：sudo sysctl -w vm.max_map_count=262144
    解决方式：https://blog.csdn.net/qq_43268365/article/details/88234308
####NoNodeAvailableException[None of the configured nodes are available
    cluster-name和cluster-nodes要配置正确