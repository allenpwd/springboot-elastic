### 问题
#### Failed: 1: type is missing
看看是否elasticsearch版本比较旧，因为es新版本不能有type了，所以升级es到7.X版本即可。
spring-boot系列的es属性是elasticsearch.version